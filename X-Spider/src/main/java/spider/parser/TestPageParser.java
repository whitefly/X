package spider.parser;

import com.entity.TestInfo;
import com.entity.PageParserDO;
import com.entity.TaskDO;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import spider.utils.RequestUtil;
import us.codecraft.webmagic.Page;

import java.util.List;

@Slf4j
@ToString
public class TestPageParser extends PageParser {

    @Getter
    TestInfo testInfo;

    public TestPageParser(TaskDO taskInfo, PageParserDO pageParserDO, TestInfo testInfo) {
        super(taskInfo, pageParserDO);
        this.testInfo = testInfo;
    }

    @Override
    public void process(Page page) {
        //解析目录和正文页
        if (taskInfo.getStartUrl().equals(page.getRequest().getUrl())) {
            List<String> otherUrls = RequestUtil.getLinksByField(page, pageParserDO.getPageRule());
            List<String> newsUrls = RequestUtil.getLinksByField(page, indexParser.getIndexRule());

            otherUrls.forEach(x -> testInfo.getBranches().add("下一页url:" + x));
            newsUrls.forEach(x -> testInfo.getNewsUrls().add(x));
        }

    }
}
