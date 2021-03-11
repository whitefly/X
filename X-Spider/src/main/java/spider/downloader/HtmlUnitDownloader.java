package spider.downloader;

import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.util.UrlUtils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.downloader.Downloader;
import us.codecraft.webmagic.selector.PlainText;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class HtmlUnitDownloader implements Downloader, AutoCloseable {
    private final int poolSize = 8;
    private final int waitForJs = 1000;
    private final int waitWindowsMaxTime = 10000;
    GenericObjectPool<WebWindow> webWindowPool;
    WebClient webClient;


    public class MytPooledObjectFactory extends BasePooledObjectFactory<WebWindow> {

        AtomicInteger counter = new AtomicInteger(0);

        public WebWindow create() {
            int i = counter.addAndGet(1);
            return webClient.openWindow(null, "p" + i);
        }


        @Override
        public PooledObject<WebWindow> wrap(WebWindow webWindow) {
            return new DefaultPooledObject<>(webWindow);
        }
    }

    void init() {
        //初始化webClient;
        WebClient webClient = new WebClient(BrowserVersion.CHROME);
        webClient.setJavaScriptTimeout(2000);
        webClient.getOptions().setCssEnabled(false);//是否启用CSS, 因为不需要展现页面, 所以不需要启用
        webClient.getOptions().setDownloadImages(false); //不下载图片
        webClient.setAjaxController(new NicelyResynchronizingAjaxController());//很重要，设置支持AJAX
        webClient.getOptions().setActiveXNative(false);
        webClient.getOptions().setThrowExceptionOnScriptError(false);//当JS执行出错的时候是否抛出异常, 这里选择不需要
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);//当HTTP的状态非200时是否抛出异常, 这里选择不需要
        webClient.waitForBackgroundJavaScript(waitForJs);
        this.webClient = webClient;

        //设置window池
        GenericObjectPoolConfig<WebWindow> conf = new GenericObjectPoolConfig<>();
        conf.setMaxTotal(poolSize);
        conf.setMaxIdle(poolSize);
        conf.setMinEvictableIdleTimeMillis(-1);
        webWindowPool = new GenericObjectPool<>(new MytPooledObjectFactory());
    }

    public HtmlUnitDownloader() {
        init();
    }

    @Override
    public Page download(Request request, Task task) {
        WebWindow webWindow = null;
        Page page = new Page();
        try {
            String url = request.getUrl();
            webWindow = webWindowPool.borrowObject(waitWindowsMaxTime);
            log.debug("向浏览器对象池 获得 windows:{}", webWindow.getName());
            URL url1 = UrlUtils.toUrlUnsafe(url);

            WebRequest r = new WebRequest(url1, webClient.getBrowserVersion().getHtmlAcceptHeader(), webClient.getBrowserVersion().getAcceptEncodingHeader());
            r.setCharset(StandardCharsets.UTF_8);


            HtmlPage p;
            log.info("downloading page " + request.getUrl());
            p = webClient.getPage(webWindow, r);
            String content = p.asXml();

            page.setRawText(content);
            page.setUrl(new PlainText(request.getUrl()));
            page.setRequest(request);
            return page;
        } catch (Exception e) {
            log.info("动态下载网页失败:" + request.getUrl(), e);
        } finally {
            if (webWindow != null) {
                webWindowPool.returnObject(webWindow);
                log.debug("向浏览器对象池  返回 windows:{}", webWindow.getName());
            }
        }
        return new Page();
    }

    @Override
    public void close() {
        if (webWindowPool != null) {
            webWindowPool.close();
        }
        if (webClient != null) {
            webClient.close();
        }
    }

    @Override
    public void setThread(int threadNum) {

    }
}
