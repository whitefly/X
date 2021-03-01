package com.dao;

import com.entity.*;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

import java.util.List;


@Repository
public class MongoDao {
    static final int ARTICLE_PAGE_COUNT = 20; //每页显示20条抓取信息

    @Autowired
    private MongoTemplate mongoTemplate;


    /**
     * 保存task
     *
     * @param task
     */
    public void saveTask(TaskDO task) {
        mongoTemplate.insert(task);
    }

    /**
     * 保存Field
     *
     * @param field
     */
    public void saveField(FieldDO field) {
        mongoTemplate.insert(field);
    }

    /**
     * 保存DispatchLog
     *
     * @param dispatchLog
     */
    public void saveDispatchLog(DispatchLogDO dispatchLog) {
        mongoTemplate.insert(dispatchLog);
    }

    /**
     * 保存savaCrawlLog
     *
     * @param crawlLog
     */
    public void savaCrawlLog(CrawlLogDO crawlLog) {
        mongoTemplate.insert(crawlLog);
    }

    /**
     * 保存Article
     *
     * @param article
     */
    public void saveArticle(ArticleDO article) {
        mongoTemplate.insert(article);
    }

    /**
     * 保存task的修改记录
     *
     * @param auditDO
     */
    public void saveAudit(AuditDO auditDO) {
        mongoTemplate.insert(auditDO);
    }

    /**
     * 通用save方法
     *
     * @param indexParserDO
     */
    public void saveIndexParser(IndexParserDO indexParserDO) {
        mongoTemplate.insert(indexParserDO);
    }

    /**
     * 根据任务名查询任务
     *
     * @param taskName
     * @return
     */
    public TaskDO findTaskByName(String taskName) {
        Query query = new Query(Criteria.where("name").is(taskName));
        TaskDO one = mongoTemplate.findOne(query, TaskDO.class);
        return one;
    }

    /**
     * 根据初始url和任务名查询任务
     *
     * @param startUrl
     * @param taskName
     * @return
     */
    public List<TaskDO> findTaskByUrl(String startUrl, String taskName) {
        Query query = new Query();
        Criteria c = new Criteria();
        query.addCriteria(c.orOperator(
                Criteria.where("name").is(taskName),
                Criteria.where("start_url").is(startUrl)));
        return mongoTemplate.find(query, TaskDO.class);
    }


    /**
     * 根据任务id查询
     *
     * @param id
     * @return
     */
    public TaskDO findTaskById(String id) {
        return mongoTemplate.findById(id, TaskDO.class);
    }


    /**
     * 查询全部任务
     *
     * @return
     */
    public List<TaskDO> findAll() {
        return mongoTemplate.findAll(TaskDO.class);
    }

    /**
     * 查询激活的全部任务
     *
     * @return
     */
    public List<TaskDO> findAllTaskActive() {
        Query query = new Query(Criteria.where("is_active").is(true));
        return mongoTemplate.find(query, TaskDO.class);
    }

    public boolean updateTaskState(TaskDO taskDO, boolean active) {
        Query query = new Query(Criteria.where("id").is(taskDO.getId()));
        Update update = new Update().set("is_active", active);
        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, TaskDO.class);
        return updateResult.wasAcknowledged();
    }

    public boolean delTask(TaskDO task) {
        Query query = new Query(Criteria.where("id").is(task.getId()));
        DeleteResult remove = mongoTemplate.remove(query, TaskDO.class);
        return remove.getDeletedCount() >= 1;
    }

    public void updateTask(TaskDO task) {
        mongoTemplate.save(task);
    }

    //-------------------------------------------------------------------------------
    //Parser表
    public IndexParserDO findIndexParserById(String id) {
        return mongoTemplate.findById(id, IndexParserDO.class);
    }

    public void updateIndexParser(IndexParserDO indexParser) {
        mongoTemplate.save(indexParser);
    }

    public boolean delIndexParser(String id) {
        Query query = new Query(Criteria.where("id").is(id));
        DeleteResult remove = mongoTemplate.remove(query, IndexParserDO.class);
        return remove.getDeletedCount() >= 1;
    }

    /**
     * parser表里有很多不同类型,需要使用泛型来解析
     *
     * @return
     */
    public Object findIndexParserById(String id, Class<? extends IndexParserDO> t) {
        return mongoTemplate.findById(id, t);
    }

    //--------------------------------------------------------------------
    //article表
    public List<ArticleDO> findArticleByPage(String taskId, Integer page) {
        Query query = taskId == null ? new Query() : new Query(Criteria.where("task_id").is(taskId));
        query.limit(ARTICLE_PAGE_COUNT).skip((page - 1) * ARTICLE_PAGE_COUNT).with(Sort.by(Sort.Direction.DESC, "ctime"));
        return mongoTemplate.find(query, ArticleDO.class);
    }

    public void deleteArticleByTaskId(String taskId) {
        Query query = taskId == null ? new Query() : new Query(Criteria.where("task_id").is(taskId));
        mongoTemplate.remove(query, ArticleDO.class);
    }

    public Long sizeOfArticle(String taskId) {
        Query query = taskId == null ? new Query() : new Query(Criteria.where("task_id").is(taskId));
        return mongoTemplate.count(query, ArticleDO.class);
    }

    //--------------------------------------------------------------------
    //log_crawl表

    /**
     * CrawlLog的分页查询
     *
     * @param taskId
     * @param page
     * @return
     */
    public List<CrawlLogDO> findAllCrawlLog(String taskId, Integer page) {
        Query query = taskId == null ? new Query() : new Query(Criteria.where("task_id").is(taskId));
        query.limit(ARTICLE_PAGE_COUNT).skip((page - 1) * ARTICLE_PAGE_COUNT).with(Sort.by(Sort.Direction.DESC, "start"));
        return mongoTemplate.find(query, CrawlLogDO.class);
    }

    /**
     * 任务的最近启动时间(聚合函数)
     *
     * @param taskIds
     * @return
     */
    public List<CrawlLogDO> findLastCrawlLog(List<String> taskIds) {
        TypedAggregation<CrawlLogDO> agg = newAggregation(CrawlLogDO.class,
                match(Criteria.where("taskId").in(taskIds)),
                group("taskId")
                        .max("startTime").as("start")
                        .first("taskId").as("task_id")
        );
        AggregationResults<CrawlLogDO> rnt = mongoTemplate.aggregate(agg, CrawlLogDO.class);
        return rnt.getMappedResults();
    }

    //--------------------------------------------------------------
    //log_dispatcher表
    public List<DispatchLogDO> findAllDispatchLog(String taskId, Integer page) {
        Query query = (taskId == null) ? new Query() : new Query(Criteria.where("task_id").is(taskId));
        query.limit(ARTICLE_PAGE_COUNT).skip((page - 1) * ARTICLE_PAGE_COUNT).with(Sort.by(Sort.Direction.DESC, "ctime"));
        return mongoTemplate.find(query, DispatchLogDO.class);
    }
}
