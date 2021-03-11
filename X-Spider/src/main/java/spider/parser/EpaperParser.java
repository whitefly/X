package spider.parser;

import com.entity.ArticleDO;
import com.entity.EpaperParserDO;
import com.entity.TaskDO;
import lombok.Getter;
import lombok.Setter;
import spider.utils.NewsParserUtil;
import spider.utils.RequestUtil;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;

import java.util.List;

//电子报爬虫
public class EpaperParser extends IndexParser {


    @Getter
    EpaperParserDO epaperParser;


    @Getter
    @Setter
    String firstUrl; //taskInfo的地址是个模板地址,无法判断

    public EpaperParser(TaskDO taskInfo, EpaperParserDO epaperParser) {
        super(taskInfo, epaperParser);
        this.epaperParser = epaperParser;
    }

    @Override
    public void process(Page page) {
        page.setSkip(true);
        if (page.getUrl().toString().equals(firstUrl)) {
            //layout页面,本身也为目录页面
            List<Request> otherUrls = RequestUtil.extractAliasRequest(page, epaperParser.getLayoutRule(), INDEX_URL_ALIAS);
            List<Request> newsUrls = RequestUtil.extractAliasRequest(page, indexParser.getIndexRule(), NEWS_URL_ALIAS);

            //通过redis忽略过去启动任务时的正文页;
            newsUrls = filterNewsRequest(newsUrls);

            //加入到队列
            RequestUtil.addToQueue(page, newsUrls);
            RequestUtil.addToQueue(page, otherUrls);
        } else {
            super.process(page);
        }
    }

    @Override
    public Site getSite() {
        return super.getSite();
    }

    public List<String> parserLayout(Page page, EpaperParserDO epaperParser) {
        return RequestUtil.getLinksByField(page, epaperParser.getLayoutRule());
    }

    @Override
    public String toString() {
        return "EpaperParser{" +
                "epaperParser=" + epaperParser +
                ", indexParser=" + indexParser +
                ", taskInfo=" + taskInfo +
                ", newsParserDO=" + newsParserDO +
                '}';
    }
}

