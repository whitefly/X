package spider.parser;

import com.entity.AjaxParserDO;
import com.entity.TaskDO;
import com.entity.TestInfo;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import spider.utils.RequestUtil;
import us.codecraft.webmagic.Page;

import java.util.List;

@Slf4j
@ToString
public class TestAjaxParser extends AjaxParser {

    @Getter
    TestInfo testInfo;

    public TestAjaxParser(TaskDO taskInfo, AjaxParserDO ajaxParserDO, TestInfo indexUrls) {
        super(taskInfo, ajaxParserDO);
        this.testInfo = indexUrls;
    }

    @Override
    public void process(Page page) {
        List<String> linksByAjax = RequestUtil.getLinksByAjax(page, ajaxParserDO.getIndexRule());
        testInfo.getNewsUrls().addAll(linksByAjax);
    }
}
