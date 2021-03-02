package com.web.service;

import com.dao.MongoDao;
import com.dao.RedisDao;
import com.entity.ArticleDO;
import com.exception.WebException;
import com.utils.RedisConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

import static com.exception.ErrorCode.SERVICE_DOC_MISS_TASK_ID;

@Service
@Slf4j
public class DocService {
    @Autowired
    MongoDao mongoDao;

    @Autowired
    RedisDao redisDao;

    /**
     * 获取某页的文章
     *
     * @return
     */
    public List<ArticleDO> docList(String taskId, int pageIndex) {
        return mongoDao.findArticleByPage(taskId, pageIndex);
    }


    public void clearDoc(String taskId) {
        if (StringUtils.isEmpty(taskId)) throw new WebException(SERVICE_DOC_MISS_TASK_ID);
        mongoDao.deleteArticleByTaskId(taskId);
        log.info("mongodb删除新闻数据[success]:{}", taskId);

        //删除redis中的指纹set
        String hashKey = RedisConstant.getHashKey(taskId);
        redisDao.delSet(hashKey);
        log.info("删除redis set:" + hashKey);
    }
}
