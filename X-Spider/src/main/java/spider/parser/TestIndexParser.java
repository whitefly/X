package spider.parser;

import com.entity.IndexParserDO;
import com.entity.TaskDO;
import lombok.Getter;
import us.codecraft.webmagic.Page;

import java.util.List;

/**
 * 用于目录项目的xpath测试
 */
public class TestIndexParser extends IndexParser {


    @Getter
    List<String> indexUrls;


    public TestIndexParser(TaskDO taskInfo, IndexParserDO indexParser, List<String> indexUrls) {
        super(taskInfo, indexParser);
        this.indexUrls = indexUrls;
    }

    @Override
    public void process(Page page) {
        //一级页面的页面
        indexUrls.addAll(parseIndexPage(page, indexParser));
    }
}
