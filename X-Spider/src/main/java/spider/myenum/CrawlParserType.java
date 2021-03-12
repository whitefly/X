package spider.myenum;

import lombok.Getter;
import spider.parser.*;

/**
 * 具体模板 和 对应的测试模板
 */
public enum CrawlParserType {
    IndexParser(spider.parser.IndexParser.class, TestIndexParser.class),
    电子报Parser(EpaperParser.class, TestEpaperParser.class),
    PageParser(PageParser.class, TestPageParser.class),
    CustomParser(CustomParser.class, TestCustomParser.class),
    AjaxParser(AjaxParser.class, TestAjaxParser.class);

    @Getter
    private final Class<? extends NewsParser> parser;

    @Getter
    private final Class<? extends NewsParser> testParser;

    CrawlParserType(Class<? extends NewsParser> parser, Class<? extends NewsParser> testParser) {
        this.parser = parser;
        this.testParser = testParser;
    }
}
