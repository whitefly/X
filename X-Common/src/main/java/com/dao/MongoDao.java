package com.dao;

import com.entity.*;
import com.mongodb.client.result.DeleteResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;


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
     * @param parserConfig
     */
    public void saveNewsParser(NewsParserDO parserConfig) {
        mongoTemplate.insert(parserConfig);
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
     * 查询任务,俺时间排序
     * Query query = taskId == null ? new Query() : new Query(Criteria.where("task_id").is(taskId));
     * query.limit(pageSize).skip((pageIndex - 1) * pageSize).with(Sort.by(Sort.Direction.DESC, "ctime"));
     * return mongoTemplate.find(query, ArticleDO.class);
     */
    public List<TaskDO> findTasksByPageIndex(Integer pageIndex, Integer pageSize, String keyWord, String parseType) {
        Query query = StringUtils.isEmpty(keyWord) ? new Query() : new Query(Criteria.where("name").regex(keyWord));
        if (!StringUtils.isEmpty(parseType)) {
            query.addCriteria(Criteria.where("parser_type").regex(parseType));
        }
        query.limit(pageSize).skip((pageIndex - 1) * pageSize).with(Sort.by(Sort.Direction.DESC, "op_time"));
        return mongoTemplate.find(query, TaskDO.class);
    }

    public long taskCount(String name, String parseType) {
        Query query = new Query();
        Criteria c = null;
        if (!StringUtils.isEmpty(name)) {
            c = Criteria.where("name").regex(name);
        }
        if (!StringUtils.isEmpty(parseType)) {
            if (c != null) c.andOperator(Criteria.where("parser_type").regex(parseType));
            else {
                c = Criteria.where("parser_type").regex(parseType);
            }
        }
        if (c != null) query.addCriteria(c);
        return mongoTemplate.count(query, TaskDO.class);
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
    public NewsParserDO findNewsParserById(String id) {
        return mongoTemplate.findById(id, NewsParserDO.class);
    }

    public List<NewsParserDO> findNewsParserList() {
        return mongoTemplate.find(new Query(), NewsParserDO.class);
    }

    public void updateNewsParser(NewsParserDO indexParser) {
        mongoTemplate.save(indexParser);
    }

    public boolean delNewsParser(String id) {
        Query query = new Query(Criteria.where("id").is(id));
        DeleteResult remove = mongoTemplate.remove(query, NewsParserDO.class);
        return remove.getDeletedCount() >= 1;
    }

    /**
     * parser表里有很多不同类型,需要使用泛型来解析
     *
     * @return
     */
    public Object findNewsParserById(String id, Class<? extends IndexParserDO> t) {
        return mongoTemplate.findById(id, t);
    }

    //--------------------------------------------------------------------
    //article表
    public List<ArticleDO> findArticleByPage(String taskId, String keyword, Integer pageIndex, int pageSize) {
        Query query = StringUtils.isEmpty(taskId) ? new Query() : new Query(Criteria.where("task_id").is(taskId));
        if (!StringUtils.isEmpty(keyword)) {
            query.addCriteria(Criteria.where("title").regex(keyword).orOperator(Criteria.where("content").regex(keyword)));
        }
        query.limit(pageSize).skip((pageIndex - 1) * pageSize).with(Sort.by(Sort.Direction.DESC, "ctime"));
        return mongoTemplate.find(query, ArticleDO.class);
    }


    public List<ArticleDO> findArticleById(String taskId) {
        Query query = taskId == null ? new Query() : new Query(Criteria.where("task_id").is(taskId));
        return mongoTemplate.find(query, ArticleDO.class);
    }

    public long countArticle(String taskId, String keyword) {
        Query query = StringUtils.isEmpty(taskId) ? new Query() : new Query(Criteria.where("task_id").is(taskId));
        if (!StringUtils.isEmpty(keyword)) {
            query.addCriteria(Criteria.where("title").regex(keyword).orOperator(Criteria.where("content").regex(keyword)));
        }
        return mongoTemplate.count(query, ArticleDO.class);
    }

    public void deleteArticleByTaskId(String taskId) {
        Query query = taskId == null ? new Query() : new Query(Criteria.where("task_id").is(taskId));
        mongoTemplate.remove(query, ArticleDO.class);
    }

    public Long sizeOfArticle(String taskId) {
        Query query = taskId == null ? new Query() : new Query(Criteria.where("task_id").is(taskId));
        return mongoTemplate.count(query, ArticleDO.class);
    }

    public ArticleDO findArticleByArticleId(String articleId) {
        return mongoTemplate.findById(articleId, ArticleDO.class);
    }

    /**
     * 统计7天的数据量
     */
    public List<DataPlus> statisticalCount() {
        long l = new Date().getTime() - 7 * 24 * 3600 * 1000;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String format = simpleDateFormat.format(new Date(l));
        Date past = new Date();
        try {
            past = simpleDateFormat.parse(format);
        } catch (ParseException e) {
            e.printStackTrace();
        }


        TypedAggregation<ArticleDO> agg = newAggregation(ArticleDO.class,
                match(Criteria.where("ctime").gte(past)),
                Aggregation.project("ctime").andExpression("{$dateToString:{format:'%Y-%m-%d', date: '$ctime', timezone: 'Asia/Shanghai'}}").as("time"),
                group("time").count().as("count"),
                Aggregation.project("time", "count").and("time").previousOperation(),
                Aggregation.sort(Sort.Direction.ASC, "time")
        );
        AggregationResults<DataPlus> rnt = mongoTemplate.aggregate(agg, DataPlus.class);
        return rnt.getMappedResults();
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
