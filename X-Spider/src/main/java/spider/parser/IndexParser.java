package spider.parser;

import com.constant.RedisConstant;
import com.dao.RedisDao;
import com.entity.FieldDO;
import com.entity.IndexParserDO;
import com.entity.TaskDO;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import spider.utils.RequestUtil;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class IndexParser extends NewsParser {


    @Getter
    IndexParserDO indexParser;


    @Setter
    RedisDao redisDao; //用来在访问redis前,扫一下是否之前访问过.若没有注入进来,就说明不需要


    private Site site = Site
            .me()
            .setSleepTime(0)
            .setUserAgent(
                    "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_2) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.65 Safari/537.31");


    public IndexParser(TaskDO taskInfo, IndexParserDO indexParser) {
        super(taskInfo, indexParser);
        this.indexParser = indexParser;
    }

    @Override
    public void process(Page page) {
        if (taskInfo.getStartUrl().equals(page.getUrl().toString())) {
            //一级页面
            List<String> urls = parseIndexPage(page, indexParser);
            urls = filterNewsUrl(urls);
            if (!CollectionUtils.isEmpty(urls)) page.addTargetRequests(urls);
            page.setSkip(true);
        } else {
            //正文页面,交给正文处理器搞定
            super.process(page);
        }
    }

    @Override
    public Site getSite() {
        return site;
    }

    public List<String> parseIndexPage(Page page, IndexParserDO parserDetail) {
        return RequestUtil.getLinksByField(page, parserDetail.getIndexRule());
    }


    public List<String> filterNewsUrl(List<String> newsUrl) {
        if (redisDao == null || CollectionUtils.isEmpty(newsUrl)) return newsUrl;

        //若这个url被当做正文页解析过,则忽略它
        String key = RedisConstant.getExtractedKey(taskInfo.getId());
        List<String> collect = newsUrl.stream().filter(url -> !redisDao.existInSet(key, url)).collect(Collectors.toList());
        int ignoreCount = newsUrl.size() - collect.size();
        log.info("{}:待访问url集合中发现过去已被提取正文的正文URL {} 个...跳过", taskInfo.getName(), ignoreCount);
        return collect;
    }

    public List<Request> filterNewsRequest(List<Request> newsRequest) {
        if (redisDao == null || CollectionUtils.isEmpty(newsRequest)) return newsRequest;
        //若这个url被当做正文页解析过,则忽略它
        String key = RedisConstant.getExtractedKey(taskInfo.getId());
        List<Request> collect = newsRequest.stream().filter(r -> !redisDao.existInSet(key, r.getUrl())).collect(Collectors.toList());
        int ignoreCount = newsRequest.size() - collect.size();
        log.info("{}:待访问url集合中发现过去已被提取正文的正文URL {} 个...跳过", taskInfo.getName(), ignoreCount);
        return collect;
    }
}
