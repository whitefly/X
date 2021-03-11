package spider.parser;

import com.entity.TestInfo;
import com.entity.EpaperParserDO;
import com.entity.TaskDO;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import spider.utils.RequestUtil;
import us.codecraft.webmagic.Page;

import java.util.List;

/**
 * 用于电子版的版面测试
 */
@Slf4j
public class TestEpaperParser extends EpaperParser {


    @Getter
    TestInfo testInfo;


    public TestEpaperParser(TaskDO taskInfo, EpaperParserDO epaperParser, TestInfo testInfo) {
        super(taskInfo, epaperParser);
        this.testInfo = testInfo;
    }

    @Override
    public void process(Page page) {
        //从初始页解析到其他目录页作为分支+本次的新闻作为即可
        if (firstUrl == null) {
            log.error("未设置电子报firstUrl,任务启动失败");
            return;
        }
        if (firstUrl.equals(page.getRequest().getUrl())) {
            List<String> otherUrls = RequestUtil.getLinksByField(page, epaperParser.getLayoutRule());
            List<String> newsUrls = RequestUtil.getLinksByField(page, indexParser.getIndexRule());

            otherUrls.forEach(x -> testInfo.getBranches().add("版面url:" + x));
            newsUrls.forEach(x -> testInfo.getNewsUrls().add(x));
        }
    }
}
