package center.web.service;

import com.dao.MongoDao;
import com.dao.RedisDao;
import com.entity.ArticleDO;
import center.exception.WebException;
import com.constant.RedisConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static center.exception.ErrorCode.SERVICE_DOC_MISS_TASK_ID;

@Service
@Slf4j
public class DocService {
    @Autowired
    MongoDao mongoDao;

    @Autowired
    RedisDao redisDao;


    public List<ArticleDO> getDocByTaskId(String taskId, String keyword, int pageIndex, int pageSize) {
        return mongoDao.findArticleByPage(taskId, keyword, pageIndex, pageSize);
    }

    public List<ArticleDO> getDocByTaskId(String taskId) {
        return mongoDao.findArticleById(taskId);
    }

    public long getDocCountByTaskId(String taskId, String keyword) {
        return mongoDao.countArticle(taskId, keyword);
    }

    public List<String> getPicUrlByArticleId(String articleId) {
        ArticleDO news = mongoDao.findArticleByArticleId(articleId);
        Map<String, Object> extra = news.getExtra();

        Pattern urlPat = Pattern.compile("(https?.+?\\.(jpg|png|jpeg|gif))");
        List<String> urls = new ArrayList<>();
        for (Map.Entry<String, Object> item : extra.entrySet()) {
            Object value = item.getValue();
            if (value instanceof String) {
                Matcher matcher = urlPat.matcher((String) value);
                while (matcher.find()) {
                    urls.add(matcher.group());
                }
            }
        }
        //替换为img标签
        List<String> collect = urls.stream().map(x -> {
            Matcher matcher = urlPat.matcher(x);
            return matcher.replaceAll("<img src='$1'>");
        }).collect(Collectors.toList());

        return collect;
    }


    public void clearDoc(String taskId) {
        if (StringUtils.isEmpty(taskId)) throw new WebException(SERVICE_DOC_MISS_TASK_ID);
        mongoDao.deleteArticleByTaskId(taskId);
        log.info("mongodb删除新闻数据[success]:{}", taskId);

        //删除redis中的指纹set
        String hashKey = RedisConstant.getExtractedKey(taskId);
        redisDao.delSet(hashKey);
        log.info("删除redis set:" + hashKey);
    }
}
