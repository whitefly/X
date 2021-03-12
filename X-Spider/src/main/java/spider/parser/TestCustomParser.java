package spider.parser;

import com.entity.CustomParserDO;
import com.entity.TestInfo;
import com.entity.StepDO;
import com.entity.TaskDO;
import lombok.ToString;
import spider.utils.RequestUtil;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@ToString
public class TestCustomParser extends CustomParser {

    /**
     * 用于链接的状态转移不可知,所以这里采用解析5次,收集信息返回给前端来判断是否正常运行
     *
     * @param taskDO
     * @param customParserDO
     */

    TestInfo testInfo;
    AtomicInteger rest = new AtomicInteger(5);

    public TestCustomParser(TaskDO taskDO, CustomParserDO customParserDO, TestInfo record) {
        super(taskDO, customParserDO);
        this.testInfo = record;
    }

    @Override
    public void process(Page page) {
        if (rest.get() < 0) return;

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

        filterRequest(page);
    }

    private void filterRequest(Page page) {
        //控制最多只下载5个新页面;
        int size = page.getTargetRequests().size();
        int i = rest.get();
        if (i >= size) {
            if (!rest.compareAndSet(i, i - size)) {
                //设置失败,就将新链接直接丢弃,成功就加入下载队列
                page.getTargetRequests().clear();
            }
        } else if (i > 0) {
            //请求过多,尝试挑出几个下载
            if (rest.compareAndSet(i, 0)) {
                List<Request> collect = IntStream.range(0, i).boxed().map(index -> page.getTargetRequests().get(index)).collect(Collectors.toList());
                page.getTargetRequests().clear();
                collect.forEach(page::addTargetRequest);
            }
        } else {
            page.getTargetRequests().clear();
        }
    }

    private void hookData(Page page, StepDO stepDO, String branch) {
        //记录解析的正文页
        if (stepDO.isExtract()) {
            testInfo.getNewsUrls().add(page.getRequest().getUrl());
        }
        //记录分支
        testInfo.getBranches().add(branch);
    }


}
