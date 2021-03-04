package spider.parser;

import com.entity.EpaperParserDO;
import com.entity.TaskDO;
import lombok.Getter;
import us.codecraft.webmagic.Page;

import java.util.List;

/**
 * 用于电子版的版面测试
 */
public class TestLayoutParser extends EpaperParser {


    @Getter
    List<String> indexUrls;


    public TestLayoutParser(TaskDO taskInfo, EpaperParserDO epaperParser, List<String> indexUrls) {
        super(taskInfo, epaperParser);
        this.indexUrls = indexUrls;
    }

    @Override
    public void process(Page page) {
        //一级页面的页面
        indexUrls.addAll(parserLayout(page, epaperParser));
    }
}
