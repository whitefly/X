package com.web.controller;

import com.entity.CrawlNode;
import com.entity.ResponseVO;
import com.manager.NodeManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@Slf4j
@RestController
@CrossOrigin
@RequestMapping("/api/nodes")
public class NodeController {

    @Autowired
    NodeManager nodeManager;

    @PostMapping(path = "/all")
    public ResponseVO getAllNodes() {
        Set<CrawlNode> nodes = nodeManager.getNodes();
        return new ResponseVO(nodes);
    }
}
