package center.utils;

import com.alibaba.excel.EasyExcel;
import com.entity.ArticleDO;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.helper.StringUtil;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class DocUtil {
    static final int LEN_LIMIT = 30;




    /**
     * 简化标题或者文字长度,用于前端显示
     *
     * @param content
     * @return
     */
    public static String simplify(String content) {
        if (StringUtils.isEmpty(content) || content.length() <= LEN_LIMIT) return content;
        return content.substring(0, LEN_LIMIT) + "...";
    }

    public static void convertToExcel(HttpServletResponse response, List<ArticleDO> data) {
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
        String fileName = null;
        try {
            fileName = URLEncoder.encode("下载数据", "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

            List<List<String>> headObj = new ArrayList<List<String>>();
            List<List<Object>> dataObj = new ArrayList<List<Object>>();
            if (CollectionUtils.isEmpty(data)) return;

            //必要字段
            ArticleDO first = data.get(0);
            List<String> titleCol = new ArrayList<>();
            titleCol.add("标题");
            List<String> contentCol = new ArrayList<>();
            contentCol.add("内容");
            List<String> ptimeCol = new ArrayList<>();
            ptimeCol.add("日期");
            headObj.add(titleCol);
            headObj.add(contentCol);
            headObj.add(ptimeCol);

            //其他字段
            Map<String, Object> extra = first.getExtra();
            extra.forEach((k, v) -> {
                List<String> newCol = new ArrayList<>();
                newCol.add(k);
                headObj.add(newCol);
            });

            //填充数据
            for (ArticleDO item : data) {
                List<Object> row = new ArrayList<>();
                //要与标题的顺序一致
                row.add(item.getTitle());
                row.add(item.getContent());
                row.add(item.getPtime());
                item.getExtra().forEach((k, v) -> row.add(v));
                dataObj.add(row);
            }
            EasyExcel.write(response.getOutputStream()).head(headObj).sheet("数据").doWrite(dataObj);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}