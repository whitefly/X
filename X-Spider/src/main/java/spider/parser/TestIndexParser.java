package spider.parser;

import com.entity.IndexParserDO;
import com.entity.TaskDO;
import com.entity.TestInfo;
import lombok.Getter;
import lombok.ToString;
import us.codecraft.webmagic.Page;

import java.util.List;

/**
 * 用于目录项目的xpath测试
 */
@ToString
public class TestIndexParser extends IndexParser {


    @Getter
    TestInfo testInfo;


    public TestIndexParser(TaskDO taskInfo, IndexParserDO indexParser, TestInfo indexUrls) {
        super(taskInfo, indexParser);
        this.testInfo = indexUrls;
    }

    @Override
    public void process(Page page) {
        //一级页面的页面
        List<String> newUrls = parseIndexPage(page, indexParser);
        testInfo.getNewsUrls().addAll(newUrls);
    }
}
