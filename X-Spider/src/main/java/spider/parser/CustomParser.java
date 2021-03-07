package spider.parser;

import com.entity.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class CustomParser extends NewsParser {
    //初始url的别名就是叫 首页,需要和前端对齐
    private static final String firstAlias = "首页";

    CustomParserDO customParser;

    public CustomParser(TaskDO taskDO, CustomParserDO customParserDO) {
        super(taskDO, customParserDO);
        customParser = customParserDO;
        customParser.initMap();
    }


    @Override
    public void process(Page page) {
        page.setSkip(true);
        if (taskInfo.getStartUrl().equals(page.getUrl().toString())) {
            //设置一个标记即可,这里的代码只会执行一次
            page.getRequest().putExtra("alias", firstAlias);
        }

        String alias = (String) IndexParser.getAlias(page.getRequest());
        StepDO stepDO = customParser.getStepDOMap().get(alias);
        if (stepDO != null) {
            executeOneStep(page, stepDO);
        }
    }

    private void executeOneStep(Page page, StepDO step) {
        if (step.isExtract()) {
            ArticleDO articleDO = NewsParser.parseArticle(page, customParser);
            page.putField("ArticleDO", articleDO);
            page.setSkip(false);
        }
        //解析成其他别名链接
        List<FieldDO> links = step.getLinks();
        Map<String, Integer> result = new HashMap<>();
        for (FieldDO f : links) {
            int count = 0;
            if (!StringUtils.isEmpty(f.getAlias())) {
                List<Request> aliasLinks = IndexParser.getAliasLinksByField(page, f, f.getAlias());
                aliasLinks.forEach(page::addTargetRequest);
                count += aliasLinks.size();
            }
            result.put(f.getAlias(), count);
        }
        log.info("类型[{}]:{}  下一步为:{}", step.getAlias(), page.getRequest().getUrl(), result);
    }

    @Override
    public Site getSite() {
        return super.site;
    }
}
