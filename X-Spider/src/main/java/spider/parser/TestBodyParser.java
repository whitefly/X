package spider.parser;

import com.entity.ArticleDO;
import com.entity.IndexParserDO;
import com.entity.TaskDO;
import spider.utils.HtmlUtil;
import lombok.Getter;
import us.codecraft.webmagic.Page;

import java.util.Map;

/**
 * 用来测试正文抓取情况的
 */
public class TestBodyParser extends IndexParser {

    @Getter
    Map<String, Object> item;

    public TestBodyParser(TaskDO taskInfo, IndexParserDO indexParser, Map<String, Object> item) {
        super(taskInfo, indexParser);
        this.item = item;
    }

    //
    @Override
    public void process(Page page) {
        //此时传入的一定是正文页url
        //二级页面
        ArticleDO articleDO = parseArticle(page, indexParser);
        item.put("title", articleDO.getTitle());
        item.put("content", articleDO.getContent());
        item.put("ptime", articleDO.getPtime());
        item.put("ctime", articleDO.getCtime());

        if (indexParser.getExtra() != null) {
            item.putAll(parseExtra(page, indexParser));
        }
    }
}
