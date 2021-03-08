package spider.pipeline;

import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

public class NothingPipeline implements Pipeline {
    //啥都不做,因为webMagic不传入Pipeline就默认加入ConsolePipeline,导致全屏的信息

    @Override
    public void process(ResultItems resultItems, Task task) {

    }
}
