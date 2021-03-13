package spider.reactor;

import lombok.Getter;
import lombok.Setter;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;

public class HookSpider extends Spider {
    /**
     * create a spider with pageProcessor.
     *
     * @param pageProcessor pageProcessor
     */

    private Runnable actionAfterStop;
    private Runnable actionBeforeStart;

    @Setter
    @Getter
    public boolean forceStop = false;

    public static HookSpider create(PageProcessor pageProcessor) {
        return new HookSpider(pageProcessor);
    }


    public HookSpider(PageProcessor pageProcessor) {
        super(pageProcessor);
    }


    public void setActionWhenStop(Runnable func) {
        checkIfRunning();
        actionAfterStop = func;
    }

    public void setActionWhenStart(Runnable func) {
        actionBeforeStart = func;
    }


    @Override
    public void run() {
        if (actionBeforeStart != null) actionBeforeStart.run();
        super.run();
        if (actionAfterStop != null) actionAfterStop.run();
    }

    public PageProcessor getParser() {
        return this.pageProcessor;
    }
}
