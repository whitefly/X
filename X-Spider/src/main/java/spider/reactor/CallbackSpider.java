package spider.reactor;

import lombok.Getter;
import lombok.Setter;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;

public class CallbackSpider extends Spider {
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

    public static CallbackSpider create(PageProcessor pageProcessor) {
        return new CallbackSpider(pageProcessor);
    }


    public CallbackSpider(PageProcessor pageProcessor) {
        super(pageProcessor);
    }


    public Spider setActionWhenStop(Runnable func) {
        checkIfRunning();
        actionAfterStop = func;
        return this;
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
}
