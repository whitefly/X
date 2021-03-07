package spider.parser;

import com.entity.ArticleDO;
import com.entity.EpaperParserDO;
import com.entity.TaskDO;
import lombok.Getter;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;

import java.util.List;

//电子报爬虫
public class EpaperParser extends IndexParser {


    @Getter
    EpaperParserDO epaperParser;

    public EpaperParser(TaskDO taskInfo, EpaperParserDO epaperParser) {
        super(taskInfo, epaperParser);
        this.epaperParser = epaperParser;
    }

    @Override
    public void process(Page page) {
        //多加一个layout流程,形成三级页面,非常蛋疼的去模仿回调函数
        if (taskInfo.getStartUrl().equals(page.getUrl().toString())) {
            //layout页面,设置下一级为二级页面
            List<String> urls = parserLayout(page, epaperParser);
            List<Request> requests = convert2AliasRequest(urls, "2级页面");
            requests.forEach(page::addTargetRequest);
            page.setSkip(true);
        } else if ("2级页面".equals(getAlias(page.getRequest()))) {
            List<String> urls = parseIndexPage(page, epaperParser);
            List<Request> requests = convert2AliasRequest(urls, "3级页面");
            requests.forEach(page::addTargetRequest);
            page.setSkip(true);
        } else if ("3级页面".equals(getAlias(page.getRequest()))) {
            //三级级页面
            ArticleDO articleDO = NewsParser.parseArticle(page, epaperParser);
            //把整个对象放入map中的ArticleDO中,在pipeline去存出
            page.putField("ArticleDO", articleDO);
        } else {
            page.setSkip(true);
        }
    }

    @Override
    public Site getSite() {
        return super.getSite();
    }

    public List<String> parserLayout(Page page, EpaperParserDO epaperParser) {
        return getLinksByField(page, epaperParser.getLayoutRule());
    }
}

