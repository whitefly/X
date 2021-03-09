package spider.parser;

import com.entity.CustomParserDO;
import com.entity.CustomTestInfo;
import com.entity.StepDO;
import com.entity.TaskDO;
import spider.utils.RequestUtil;
import us.codecraft.webmagic.Page;

import java.util.concurrent.atomic.AtomicInteger;

public class TestCustomParser extends CustomParser {

    /**
     * 用于链接的状态转移不可知,所以这里采用解析5次,收集信息返回给前端来判断是否正常运行
     *
     * @param taskDO
     * @param customParserDO
     */

    CustomTestInfo customTestInfo;
    AtomicInteger exeCount = new AtomicInteger(0);
    int limit = 5;

    public TestCustomParser(TaskDO taskDO, CustomParserDO customParserDO, CustomTestInfo record) {
        super(taskDO, customParserDO);
        this.customTestInfo = record;
    }

    @Override
    public void process(Page page) {
        int i = exeCount.addAndGet(1);
        if (i >= limit) {
            return;
        }

        page.setSkip(true);
        if (taskInfo.getStartUrl().equals(page.getUrl().toString())) {
            page.getRequest().putExtra("alias", FIRST_ALIAS);
        }
        String alias = (String) RequestUtil.getAlias(page.getRequest());
        StepDO stepDO = customParser.getStepDOMap().get(alias);
        if (stepDO != null) {
            String branch = executeOneStep(page, stepDO);
            //记录运行信息,返回给前端
            hookData(page, stepDO, branch);
        }
    }

    private void hookData(Page page, StepDO stepDO, String branch) {
        //记录解析的正文页
        if (stepDO.isExtract()) {
            customTestInfo.getNewsUrls().add(page.getRequest().getUrl());
        }
        //记录分支
        customTestInfo.getBranches().add(branch);
    }

}
