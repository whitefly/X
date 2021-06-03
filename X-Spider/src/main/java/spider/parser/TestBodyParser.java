package spider.parser;

import com.entity.ArticleDO;
import com.entity.NewsParserDO;
import com.entity.TaskDO;
import lombok.Getter;
import lombok.ToString;
import spider.utils.NewsParserUtil;
import us.codecraft.webmagic.Page;

import java.util.List;
import java.util.Map;

/**
 * 用来测试正文抓取情况的
 */

@ToString
public class TestBodyParser extends NewsParser {

    @Getter
    Map<String, Object> item;

    @Getter
    List<Map<String, Object>> itemList;

    public TestBodyParser(TaskDO taskInfo, NewsParserDO newsParserDO, Map<String, Object> item) {
        super(taskInfo, newsParserDO);
        this.item = item;
    }

    public TestBodyParser(TaskDO taskInfo, NewsParserDO newsParserDO, List<Map<String, Object>> itemList) {
        super(taskInfo, newsParserDO);
        this.itemList = itemList;
    }

    @Override
    public void process(Page page) {
        //此时传入的一定是正文页url
        //二级页面
        if (newsParserDO.getBlockSplit() == null || !newsParserDO.getBlockSplit()) {
            ArticleDO articleDO = NewsParserUtil.parseArticle(page, newsParserDO);
            item.put("title", articleDO.getTitle());
            item.put("content", articleDO.getContent());
            item.put("ptime", articleDO.getPtime());
            item.put("ctime", articleDO.getCtime());
            if (articleDO.getExtra() != null) {
                item.putAll(articleDO.getExtra());
            }
        } else {
            //仅仅只传递extra中的值
            List<ArticleDO> articleDOS = NewsParserUtil.parseArticlesList(page, newsParserDO);
            for (ArticleDO articleDO : articleDOS) {
                itemList.add(articleDO.getExtra());
            }
        }
    }
}
