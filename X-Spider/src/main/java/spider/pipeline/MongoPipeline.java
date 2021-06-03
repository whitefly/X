package spider.pipeline;

import com.constant.RedisConstant;
import com.dao.MongoDao;
import com.dao.RedisDao;
import com.entity.ArticleDO;
import com.entity.TaskDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import spider.parser.NewsParser;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;
import us.codecraft.webmagic.processor.PageProcessor;

import java.lang.reflect.Field;
import java.util.List;

import spider.parser.NewsParser;

@Slf4j
@Component
public class MongoPipeline implements Pipeline {

    private final MongoDao mongoDao;

    private final RedisDao redisDao;

    public MongoPipeline(MongoDao mongoDao, RedisDao redisDao) {
        this.mongoDao = mongoDao;
        this.redisDao = redisDao;
    }

    @Override
    public void process(ResultItems resultItems, Task task) {
        //为了获取TaskInfo,用了反射o(╥﹏╥)o
        TaskDO taskDO = getTaskInfo(task);
        if (taskDO == null) return;

        Object crawlResult = resultItems.get(NewsParser.ARTICLE_DO_KEY);
        if (crawlResult == null) return;

        String url = resultItems.getRequest().getUrl();
        String VisitedKey = RedisConstant.getExtractedKey(taskDO.getId());

        if (!redisDao.existInSet(VisitedKey, url)) {
            int itemSize = saveToMongo(crawlResult);
            SaveUrlToRedis(taskDO.getName(), url, VisitedKey, itemSize);
        }
    }

    private void SaveUrlToRedis(String taskName, String url, String VisitedKey, int itemSize) {
        redisDao.addSet(VisitedKey, url);
        log.info("{}:网站被提取完成[结果量:{}],存储进Redis已访问正文网页集合中 {}", taskName, itemSize, url);
    }

    private int saveToMongo(Object crawlResult) {
        if (crawlResult instanceof ArticleDO) {
            ArticleDO article = (ArticleDO) crawlResult;
            mongoDao.saveArticle(article);
            return 1;
        } else if (crawlResult instanceof List) {
            //正文页中解析多结果
            List<ArticleDO> articles = (List<ArticleDO>) crawlResult;
            mongoDao.saveArticles(articles);
            return articles.size();
        }
        return 0;
    }


    private TaskDO getTaskInfo(Task task) {
        if (task instanceof Spider) {
            try {
                Field pageProcessor = Spider.class.getDeclaredField("pageProcessor");
                pageProcessor.setAccessible(true);
                PageProcessor pageProcessor1 = (PageProcessor) pageProcessor.get(task);
                if (pageProcessor1 instanceof NewsParser) {
                    NewsParser indexParser = (NewsParser) pageProcessor1;
                    return indexParser.getTaskInfo();
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

}
