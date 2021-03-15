package com.conf;

import com.checker.IllegalWordsSearch;
import lombok.extern.slf4j.Slf4j;
import org.apache.xmlbeans.impl.common.ReaderInputStream;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class CheckerConf {
    @Bean
    public IllegalWordsSearch getIllegalWordsSearch() {
        //读取敏感词字符
        String wordFileName = "sensi_words.txt";
        IllegalWordsSearch illegalWordsSearch = new IllegalWordsSearch();
        try (InputStream resourceAsStream = CheckerConf.class.getClassLoader().getResourceAsStream(wordFileName)) {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(resourceAsStream));
            List<String> collect = bufferedReader.lines().collect(Collectors.toList());
            illegalWordsSearch.SetKeywords(collect);
            if (collect.size() != 0) {
                log.info("敏感词载入成功...敏感词checker已经被注入");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return illegalWordsSearch;
    }
}

