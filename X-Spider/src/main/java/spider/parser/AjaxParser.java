package spider.parser;

import com.entity.AjaxParserDO;
import com.entity.IndexParserDO;
import com.entity.TaskDO;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import spider.utils.RequestUtil;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;

import java.util.List;

@Slf4j
public class AjaxParser extends IndexParser {

    @Getter
    AjaxParserDO ajaxParserDO;

    public AjaxParser(TaskDO taskInfo, AjaxParserDO ajaxParserDO) {
        super(taskInfo, ajaxParserDO);
        this.ajaxParserDO = ajaxParserDO;
    }

    @Override
    public void process(Page page) {
        //最开始一个ajax
        page.setSkip(true);
        boolean isFirst = taskInfo.getStartUrl().equals(page.getUrl().toString());
        if (isFirst) {
            //获取正文页(IndexRule为jsonPath)
            List<String> linksByAjax = RequestUtil.getLinksByAjax(page, ajaxParserDO.getIndexRule());
            List<Request> requests = RequestUtil.convert2AliasRequest(linksByAjax, NEWS_URL_ALIAS);
            filterNewsRequest(requests);
            RequestUtil.addToQueue(page, requests);
        } else {
            super.process(page);
        }
    }
}
