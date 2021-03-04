package center.manager;

import com.entity.CrawlNode;

import java.util.List;

public interface Manager {


    void stop(String nodeId);


    void start(String nodeId);
}
