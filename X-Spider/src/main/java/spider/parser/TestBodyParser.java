package spider.parser;

import com.entity.ArticleDO;
import com.entity.NewsParserDO;
import com.entity.TaskDO;
import lombok.Getter;
import us.codecraft.webmagic.Page;

import java.util.Map;

/**
 * 用来测试正文抓取情况的
 */
public class TestBodyParser extends NewsParser {

    @Getter
    Map<String, Object> item;

    public TestBodyParser(TaskDO taskInfo, NewsParserDO newsParserDO, Map<String, Object> item) {
        super(taskInfo, newsParserDO);
        this.item = item;
    }

    //
    @Override
    public void process(Page page) {
        //此时传入的一定是正文页url
        //二级页面
        ArticleDO articleDO = NewsParser.parseArticle(page, newsParserDO);
        item.put("title", articleDO.getTitle());
        item.put("content", articleDO.getContent());
        item.put("ptime", articleDO.getPtime());
        item.put("ctime", articleDO.getCtime());
        item.putAll(articleDO.getExtra());
    }
}
