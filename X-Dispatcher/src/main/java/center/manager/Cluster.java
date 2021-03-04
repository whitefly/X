package center.manager;

import com.entity.CrawlNode;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;


@Data
public class Cluster {
    //集群id

    String ClusterId;

    //集群下管理的节点
    Map<String, CrawlNode> nodes = new HashMap<>();

    public Cluster(String clusterId) {
        ClusterId = clusterId;
    }
}
