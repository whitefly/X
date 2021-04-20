package spider.service;

import com.constant.RedisConstant;
import com.dao.MailDao;
import com.dao.MongoDao;
import com.dao.RedisDao;
import com.entity.ArticleDO;
import com.entity.SubscribeGroupDO;
import com.entity.TaskDO;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.wltea.analyzer.lucene.IKAnalyzer;
import spider.parser.NewsParser;
import spider.reactor.HookSpider;
import us.codecraft.webmagic.processor.PageProcessor;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class MailService {
    private final MongoDao mongoDao;
    private final RedisDao redisDao;
    private final MailDao mailDao;
    private ThreadLocal<Analyzer> analyzer = ThreadLocal.withInitial(() -> new IKAnalyzer(true));


    public MailService(MongoDao mongoDao, RedisDao redisDao, MailDao mailDao) {
        this.mongoDao = mongoDao;
        this.redisDao = redisDao;
        this.mailDao = mailDao;
    }


    public void freshMail(TaskDO task, HookSpider spider) {
        PageProcessor processor = spider.getParser();
        NewsParser parser;

        //得到本次任务启动后采集的新闻结果
        List<ArticleDO> freshNews = null;
        if (processor instanceof NewsParser) {
            parser = (NewsParser) processor;
            freshNews = parser.getFreshNews();
            if (CollectionUtils.isEmpty(freshNews)) return;
        }

        //得到每个任务包含的订阅组
        String subscribeKey = RedisConstant.getSubscribeKey(task.getId());
        Map<String, String> map = redisDao.getMap(subscribeKey);
        if (CollectionUtils.isEmpty(map)) return;

        //查询符合的订阅组
        Set<String> subscribeGroupIds = map.keySet();
        List<SubscribeGroupDO> groups = mongoDao.findGroupByIds(subscribeGroupIds);

        //给命中的订阅组发送邮件,筛选出合适的新闻
        List<ArticleDO> finalFreshNews = freshNews;
        List<Set<String>> wordsMapping = freshNews.parallelStream().map(news -> splitWord(news.getTitle())).collect(Collectors.toList());

        groups.parallelStream().forEach(group -> {
            filterAndSend(task, group, finalFreshNews, wordsMapping);
        });
    }

    private void filterAndSend(TaskDO task, SubscribeGroupDO group, List<ArticleDO> news, List<Set<String>> wordsMapping) {
        // TODO: 2021/4/20 用于没有账号系统,暂时只给管理员账号发邮件
        String userId = group.getUserId();
        if (!"admin".equals(userId)) return;

        //对新闻进行筛选
        List<ArticleDO> result = new ArrayList<>();
        List<String> wordInTitle = new ArrayList<>();
        int size = wordsMapping.size();
        for (int i = 0; i < size; i++) {
            Set<String> titleWords = wordsMapping.get(i);
            ArticleDO article = news.get(i);
            for (String kw : group.getKeywords()) {
                if (titleWords.contains(kw)) {
                    result.add(article);
                    wordInTitle.add(kw);
                    break;
                }
            }
        }
        //生成内容并发送邮件
        if (!CollectionUtils.isEmpty(result)) {
            String subject = String.format("订阅组[%s] 符合新闻个数: %d", group.getGroupName(), result.size());
            log.info("订阅组[ {} ] 符合新闻个数: {}", group.getGroupName(), result.size());
            String content = mailContent(task, result, wordInTitle);
            mailDao.sendMail("316447676@qq.com", subject, content);
        }
    }


    private String mailContent(TaskDO task, List<ArticleDO> articles, List<String> keyword) {
        StringBuilder sb = new StringBuilder();
        sb.append("任务[").append(task.getName()).append("]").append(" 初始url:[ ").append(task.getStartUrl()).append(" ]").append("\n\n");

        int size = articles.size();
        for (int i = 0; i < size; i++) {
            ArticleDO articleDO = articles.get(i);
            String kw = keyword.get(i);
            sb.append("  ").append(i + 1).append(". ").append("\n");
            sb.append("     ").append("标题: ").append(articleDO.getTitle()).append("\n");
            sb.append("     ").append("关键字: ").append(kw).append("\n");
            sb.append("     ").append("url: ").append(articleDO.getUrl()).append("\n");
            sb.append("\n");
        }
        return sb.toString();
    }

    private Set<String> splitWord(String sentence) {
        Analyzer ikAnalyzer = analyzer.get();
        TokenStream tokenStream = ikAnalyzer.tokenStream("", sentence);
        CharTermAttribute charTerm = tokenStream.getAttribute(CharTermAttribute.class);
        Set<String> words = new HashSet<>();
        try {
            tokenStream.reset();
            while (tokenStream.incrementToken()) {
                words.add(charTerm.toString());
            }
            tokenStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return words;
    }
}
