## 项目介绍

本项目是分布式通用爬虫,主要含有功能 
1. 任务创建:支持4种固定模板+自定义模板 快速创建任务
2. 任务管理:开启、暂停
3. 抓取结果:搜索、查看、下载
4. 节点管理:对抓取节点的状态查看与集群切换
5. 自动生成定位器:通过点击生成css选择器,不需要手写xpath
6. 正文提取:基于本文密度算法,自动提取新闻标题、时间、正文

项目技术栈:Vue+SpringBoot+WebMagic <br>
中间件:Zookeeper+Redis <br>
数据库:MongoDB <br>

### Web页面 

dashBoard
![dashBoard](./simple/dashBoard.png)

任务管理页面
![任务管理](./simple/taskList.png)

任务编辑界面
![任务编辑](./simple/taskCreate.png)

可视化辅助定位器
![辅助定位器](./simple/helper.png)

抓取结果管理页面
![抓取结果](./simple/docList.png)

节点管理
![节点管理](./simple/nodeList.png)

订阅组管理
![订阅组](./simple/groupList.png)
