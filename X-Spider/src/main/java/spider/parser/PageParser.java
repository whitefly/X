package spider.parser;

import com.entity.ArticleDO;
import com.entity.PageParserDO;
import com.entity.TaskDO;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;

import java.util.List;

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
            List<Request> newUrls = getAliasLinksByField(page, indexParser.getIndexRule(), "newUrls");
            List<Request> otherUrls = getAliasLinksByField(page, pageParserDO.getPageRule(), "otherUrls");

            //通过redis忽略过去启动任务时的正文页;
            newUrls = filterNewsRequest(newUrls);
            //加入框架队列中
            newUrls.forEach(page::addTargetRequest);
            otherUrls.forEach(page::addTargetRequest);
        } else if ("otherUrls".equals(getAlias(page.getRequest()))) {
            List<Request> newUrls = getAliasLinksByField(page, indexParser.getIndexRule(), "newUrls");
            List<Request> otherUrls = getAliasLinksByField(page, pageParserDO.getPageRule(), "otherUrls");

            newUrls = filterNewsRequest(newUrls);
            //加入框架队列中
            newUrls.forEach(page::addTargetRequest);
            otherUrls.forEach(page::addTargetRequest);
        } else if ("newUrls".equals(getAlias(page.getRequest()))) {
            ArticleDO articleDO = NewsParser.parseArticle(page, indexParser);

            //把整个对象放入map中的ArticleDO中,在pipeline去存出
            page.putField("ArticleDO", articleDO);
            //开启管道后序流程
            page.setSkip(false);


        }
    }
}
