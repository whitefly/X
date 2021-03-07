package spider.pipeline;

import com.dao.MongoDao;
import com.dao.RedisDao;
import com.entity.ArticleDO;
import com.entity.TaskDO;
import spider.parser.IndexParser;
import com.utils.FingerprintUtil;
import com.constant.RedisConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import spider.parser.NewsParser;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;
import us.codecraft.webmagic.processor.PageProcessor;

import java.lang.reflect.Field;

@Slf4j
@Component
public class MongoPipeline implements Pipeline {

    @Autowired
    private MongoDao mongoDao;

    @Autowired
    private RedisDao redisDao;

    @Override
    public void process(ResultItems resultItems, Task task) {
        //为了获取TaskInfo,用了反射o(╥﹏╥)o
        TaskDO taskDO = getTaskInfo(task);
        if (taskDO == null) return;
        ArticleDO article = resultItems.get("ArticleDO");
        if (article == null) return;

        String url = resultItems.getRequest().getUrl();
        String key = RedisConstant.getExtractedKey(taskDO.getId());

        if (!redisDao.existInSet(key, url)) {
            //若指纹不存在,则存入
            article.setUrl(resultItems.getRequest().getUrl());
            article.setTaskId(taskDO.getId());
            article.setTaskName(taskDO.getName());
            mongoDao.saveArticle(article);
            redisDao.addSet(key, url);
            log.info("{}:网站被提取完成,存储进redis的已访问正文网页集合中 {}", taskDO.getName(), url);
        }

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
