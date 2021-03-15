package spider.pipeline;

import com.checker.IllegalWordsSearch;
import com.checker.IllegalWordsSearchResult;
import com.mytype.NewsState;
import com.entity.ArticleDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

import java.util.Map;

@Component
@Slf4j
public class NewsHealthPipeLine implements Pipeline {

    final IllegalWordsSearch illegalWordsSearch;

    public NewsHealthPipeLine(IllegalWordsSearch illegalWordsSearch) {
        this.illegalWordsSearch = illegalWordsSearch;
    }


    @Override
    public void process(ResultItems resultItems, Task task) {
        ArticleDO article = resultItems.get("ArticleDO");

        String title = article.getTitle();
        String content = article.getContent();
        Map<String, Object> extra = article.getExtra();
        boolean extraMiss = false;

        if (extra != null) {
            for (Map.Entry<String, Object> item : extra.entrySet()) {
                if (StringUtils.isEmpty(item.getValue())) {
                    extraMiss = true;
                    break;
                }
            }
        }

        article.setState(NewsState.NORMAL.getDescribe());

        //是否有缺失或者长度异常
        if (StringUtils.isEmpty(title) || title.length() <= 3) {
            //标题过短
            article.setState(NewsState.TITLE_TOO_SHORT.getDescribe());
        } else if (title.length() > 50) {
            //标题过长
            article.setState(NewsState.TITLE_TOO_LONG.getDescribe());
        } else if (StringUtils.isEmpty(content) || content.length() <= 5) {
            //正文过短
            article.setState(NewsState.CONTENT_TOO_SHORT.getDescribe());
        } else if (extraMiss) {
            //自定义字段无数据
            article.setState(NewsState.EXTRA_MISS.getDescribe());
        } else if (illegalWordsSearch != null && illegalWordsSearch.ContainsAny(content)) {
            //含有敏感词
            IllegalWordsSearchResult illegalWordsSearchResult = illegalWordsSearch.FindFirst(content);
            String keyword = illegalWordsSearchResult.Keyword;
            log.info("含有敏感词:" + keyword);
            article.setState(NewsState.ILLEGAL_WORD_EXIST.getDescribe());
        }
    }
}
