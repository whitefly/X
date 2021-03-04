package spider.reactor;

import com.constant.ZKConstant;
import com.dao.RedisDao;
import com.entity.CrawlNodeInfo;
import com.google.gson.Gson;
import org.springframework.stereotype.Component;
import spider.monitor.SysMonitor;
import com.constant.CmdType;
import com.constant.RedisConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.concurrent.TimeUnit;

import static com.utils.SystemInfoUtil.NODE_PID;


@Slf4j
@Component
public class CommandManager {
    @Autowired
    CrawlReactor crawlReactor;

    @Autowired
    SysMonitor sysMonitor;

    @Autowired
    private RedisDao redisDao;

    Gson gson = new Gson();


    @Scheduled(cron = "*/3 * * * * ?")
    private void listenForCmd() {
        //5秒轮训一下redis中的命令list
        String cmdStr = fetchCmd();
        if (cmdStr == null) return;

        CmdType cmd = CmdType.getCmdTypeFromCmdStr(cmdStr);
        String param = CmdType.getParamFromCmdStr(cmdStr);

        if (cmd != null) {
            handleCmd(cmd, param);
        } else {
            log.error("无法识别指令: {}", cmdStr);
        }
    }

    @Scheduled(cron = "1/3 * * * * ?")
    private void uploadState() {
        String stateKey = RedisConstant.getStateKey(NODE_PID);
        CrawlNodeInfo info = genCrawlNodeState();
        redisDao.setValueOnExpire(stateKey, gson.toJson(info), 30, TimeUnit.SECONDS);
    }

    private String fetchCmd() {
        String cmdKey = RedisConstant.getCmdKey(NODE_PID);
        return redisDao.pop(cmdKey);
    }

    private CrawlNodeInfo genCrawlNodeState() {
        CrawlNodeInfo info = new CrawlNodeInfo();

        info.setWorkState(crawlReactor.isWorkState());
        info.setSysInfo(sysMonitor.getSysInfo());
        return info;
    }

    private void handleCmd(CmdType cmd, String param) {
        switch (cmd) {
            case Node_Work_Start:
                handleWorkStart();
                break;
            case Node_Work_Stop:
                handleWorkStop();
                break;
            case Node_Cluster_Move:
                handleClusterMove(param);
                break;
            case Node_Process_Kill:
                handleKill();
                break;
            default:
        }
    }

    private void handleWorkStart() {
        crawlReactor.setWorkState(true);
    }

    private void handleWorkStop() {
        crawlReactor.setWorkState(false);
    }

    private void handleClusterMove(String clusterId) {
        //修改zk,
        crawlReactor.moveNode(clusterId);

        //修改监听的任务队列名
        if (ZKConstant.Spider_Cluster_Short_ROOT.equals(clusterId)) {
            crawlReactor.changeTaskQueue(RedisConstant.DISPATCHER_SHORT_TASK_QUEUE_KEY);
        } else if (ZKConstant.Spider_Cluster_Long_ROOT.equals(clusterId)) {
            crawlReactor.changeTaskQueue(RedisConstant.DISPATCHER_LONG_TASK_QUEUE_KEY);
        }
    }

    private void handleKill() {
        System.exit(-1);
    }
}
