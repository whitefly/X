package spider.parser;

import com.entity.ArticleDO;
import com.entity.PageParserDO;
import com.entity.TaskDO;
import spider.utils.NewsParserUtil;
import spider.utils.RequestUtil;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;

import java.util.List;

/**
 * 翻页抓取流程
 */
public class PageParser extends IndexParser {

    PageParserDO pageParserDO;

    public PageParser(TaskDO taskInfo, PageParserDO pageParserDO) {
        super(taskInfo, pageParserDO);
        this.pageParserDO = pageParserDO;
    }


    @Override
    public void process(Page page) {
        page.setSkip(true);
        if (taskInfo.getStartUrl().equals(page.getUrl().toString())) {
            //处理初始页面
            List<Request> newsUrls = RequestUtil.extractAliasRequest(page, indexParser.getIndexRule(), NEWS_URL_ALIAS);
            List<Request> otherUrls = RequestUtil.extractAliasRequest(page, pageParserDO.getPageRule(), INDEX_URL_ALIAS);
            //通过redis忽略过去启动任务时的正文页;
            newsUrls = filterNewsRequest(newsUrls);
            //加入框架队列中
            RequestUtil.addToQueue(page, newsUrls);
            RequestUtil.addToQueue(page, otherUrls);
        } else {
            super.process(page);
        }
    }

    @Override
    public String toString() {
        return "PageParser{" +
                "pageParserDO=" + pageParserDO +
                ", indexParser=" + indexParser +
                ", taskInfo=" + taskInfo +
                ", newsParserDO=" + newsParserDO +
                '}';
    }
}
