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

import java.util.*;
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
        if (extra != null) {
            for (Map.Entry<String, Object> item : extra.entrySet()) {
                Object value = item.getValue();
                if (value instanceof String) {
                    urls.addAll(getImgFromStr((String) value, urlPat));
                } else if (value instanceof Collection)
                    for (Object o : (Collection) value) {
                        if (o instanceof String) {
                            urls.addAll(getImgFromStr((String) o, urlPat));
                        }
                    }
            }
        }
        //生成img标签
        List<String> imgTagStr = urls.stream().map(x -> {
            Matcher matcher = urlPat.matcher(x);
            return matcher.replaceAll("<img src='$1'>");
        }).collect(Collectors.toList());

        return imgTagStr;
    }

    private List<String> getImgFromStr(String str, Pattern urlPat) {
        if (str == null) return Collections.emptyList();
        ArrayList<String> result = new ArrayList<>();
        Matcher matcher = urlPat.matcher(str);
        while (matcher.find()) {
            result.add(matcher.group());
        }
        return result;

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

    public void delDoc(String docId) {
        ArticleDO news = mongoDao.findArticleByArticleId(docId);
        long l = mongoDao.delArticle(news.getId());
        //删除redis中的指纹
        String hashKey = RedisConstant.getExtractedKey(news.getTaskId());
        redisDao.delMember(hashKey, news.getUrl());
        log.info("删除redis set[ {} ]的成员[ {} ]", hashKey, news.getUrl());
    }
}
