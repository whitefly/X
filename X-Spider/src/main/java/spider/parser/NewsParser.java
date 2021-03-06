package spider.parser;

import com.entity.ArticleDO;
import com.entity.NewsParserDO;
import com.entity.TaskDO;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import spider.utils.NewsParserUtil;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;

import java.util.ArrayList;
import java.util.List;

/**
 * 最底层的正文处理器,其他处理器最终都会解析正文,所以都会继承它
 */
@Slf4j
public class NewsParser implements PageProcessor {

    public static final String ARTICLE_DO_KEY = "ArticleDO";
    static final String NEWS_URL_ALIAS = "正文页";

    //用来记录更新个数(更新通知的模块需要)
    @Getter
    @Setter
    List<ArticleDO> freshNews;


    Site site = Site
            .me()
            .setSleepTime(0)
            .setUserAgent(
                    "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_2) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.65 Safari/537.31");


    @Getter
    TaskDO taskInfo;

    @Getter
    NewsParserDO newsParserDO;


    public NewsParser(TaskDO taskDO, NewsParserDO newsParserDO) {
        this.taskInfo = taskDO;
        this.newsParserDO = newsParserDO;
    }

    @Override
    public void process(Page page) {

        List<ArticleDO> articleDOList = new ArrayList<>();
        boolean notBlockSplit = newsParserDO.getBlockSplit() == null || !newsParserDO.getBlockSplit();
        if (notBlockSplit) {
            articleDOList.add(NewsParserUtil.parseArticle(page, newsParserDO));
        } else {
            articleDOList = NewsParserUtil.parseArticlesList(page, newsParserDO);
        }

        //填充url+taskId
        for (ArticleDO articleDO : articleDOList) {
            articleDO.setUrl(page.getRequest().getUrl());
            articleDO.setTaskId(taskInfo.getId());
            articleDO.setTaskName(taskInfo.getName());
        }

        //把整个对象放入map中的ArticleDO中,在pipeline去存出
        if (notBlockSplit) {
            page.putField(ARTICLE_DO_KEY, articleDOList.get(0));
        } else {
            page.putField(ARTICLE_DO_KEY, articleDOList);
        }

        page.setSkip(false);

        //统计
        if (freshNews != null) {
            freshNews.addAll(articleDOList);
        }
    }


    @Override
    public Site getSite() {
        return site;
    }
}
