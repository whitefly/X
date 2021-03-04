package com.conf;

import com.checker.IllegalWordsSearch;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class CheckerConf {
    @Bean
    public IllegalWordsSearch getIllegalWordsSearch() {
        //读取敏感词字符
        IllegalWordsSearch illegalWordsSearch = new IllegalWordsSearch();
        URL resource = CheckerConf.class.getClassLoader().getResource("sensi_words.txt");
        if (resource != null) {
            try {
                BufferedReader bufferedReader = new BufferedReader(new FileReader(resource.getFile()));
                List<String> collect = bufferedReader.lines().collect(Collectors.toList());
                illegalWordsSearch.SetKeywords(collect);
                if (collect.size() != 0) {
                    log.info("敏感词载入成功...敏感词checker已经被注入");
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return illegalWordsSearch;
    }
}

