package center.web.controller;

import center.manager.ClusterManager;
import center.web.service.DocService;
import center.web.service.LogService;
import center.web.service.TaskService;
import com.entity.DataPlus;
import com.entity.DataPlusVO;
import com.entity.ResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    DocService docService;

    @Autowired
    TaskService taskService;

    @Autowired
    LogService crawlService;

    @Autowired
    ClusterManager manager;


    @PostMapping(path = "/all")
    public ResponseVO dashboardData() {
        List<DataPlus> dataPluses = crawlService.DataPlus();

        List<String> date = new ArrayList<>();
        List<Integer> count = new ArrayList<>();

        dataPluses.forEach(x -> {
            date.add(x.getTime());
            count.add(x.getCount());
        });
        DataPlusVO dataPlusVO = new DataPlusVO(date, count);
        long newsCount = docService.getDocCountByTaskId(null, null);
        long taskCount = taskService.getTaskCount();
        long nodeCount = manager.getNodeCount();


        dataPlusVO.setNewsCount(newsCount);
        dataPlusVO.setTaskCount(taskCount);
        dataPlusVO.setNodeCount(nodeCount);
        return new ResponseVO(dataPlusVO);
    }
}
