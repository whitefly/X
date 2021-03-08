package spider.utils;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReUtil {

    static Cache<String, Pattern> cache = CacheBuilder.newBuilder().maximumSize(500).build();

    public static String regex(String re, String content, boolean all) {
        //判断是否有括号
        if (re == null || content == null) return null;
        int l = re.indexOf("(");
        int r = re.indexOf(")");
        if (l == -1 || r == -1 || l >= r) {
            return null;
        }

        Pattern compile = cache.getIfPresent(re);
        if (compile == null) {
            compile = Pattern.compile(re);
            cache.put(re, compile);
        }
        Matcher matcher = compile.matcher(content);
        List<String> result = new ArrayList<>();
        if (all) {
            while (matcher.find()) {
                String group = matcher.group(1);
                result.add(group);
            }
        } else {
            if (matcher.find()) {
                result.add(matcher.group(1));
            }
        }
        return String.join(" \n ", result);
    }

    public static void main(String[] args) {
        String urls = "http://www.xiuren.org/images/2017/05/527592213.jpg \\n http://www.xiuren.org/misc/okamoto/002/0001.jpg \\n http://www.xiuren.org/misc/okamoto/002/0002.jpg \\n http://www.xiuren.org/misc/okamoto/002/0003.jpg \\n http://www.xiuren.org/misc/okamoto/002/0004.jpg \\n http://www.xiuren.org/misc/okamoto/002/0005.jpg \\n http://www.xiuren.org/misc/okamoto/002/0006.jpg \\n http://www.xiuren.org/misc/okamoto/002/0007.jpg \\n http://www.xiuren.org/misc/okamoto/002/0008.jpg \\n http://www.xiuren.org/misc/okamoto/002/0010.jpg \\n http://www.xiuren.org/misc/okamoto/002/0011.jpg \\n http://www.xiuren.org/misc/okamoto/002/0012.jpg \\n http://www.xiuren.org/misc/okamoto/002/0013.jpg \\n http://www.xiuren.org/misc/okamoto/002/0014.jpg \\n http://www.xiuren.org/misc/okamoto/002/0015.jpg \\n http://www.xiuren.org/misc/okamoto/002/0016.jpg \\n http://www.xiuren.org/misc/okamoto/002/0020.jpg \\n http://www.xiuren.org/misc/okamoto/002/0021.jpg \\n http://www.xiuren.org/misc/okamoto/002/0022.jpg \\n http://www.xiuren.org/misc/okamoto/002/0023.jpg \\n http://www.xiuren.org/misc/okamoto/002/0024.jpg \\n http://www.xiuren.org/misc/okamoto/002/0025.jpg \\n http://www.xiuren.org/misc/okamoto/002/0026.jpg \\n http://www.xiuren.org/misc/okamoto/002/0027.jpg \\n http://www.xiuren.org/misc/okamoto/002/0028.jpg \\n http://www.xiuren.org/misc/okamoto/002/0029.jpg \\n http://www.xiuren.org/misc/okamoto/002/0030.jpg \\n http://www.xiuren.org/misc/okamoto/002/0031.jpg \\n http://www.xiuren.org/misc/okamoto/002/0032.jpg \\n http://www.xiuren.org/misc/okamoto/002/0033.jpg \\n http://www.xiuren.org/misc/okamoto/002/0034.jpg \\n http://www.xiuren.org/misc/okamoto/002/0035.jpg \\n http://www.xiuren.org/misc/okamoto/002/0039.jpg \\n http://www.xiuren.org/misc/okamoto/002/0040.jpg \\n http://www.xiuren.org/misc/okamoto/002/0041.jpg \\n http://www.xiuren.org/misc/okamoto/002/0042.jpg \\n http://www.xiuren.org/misc/okamoto/002/0043.jpg \\n http://www.xiuren.org/misc/okamoto/002/0044.jpg \\n http://www.xiuren.org/misc/okamoto/002/0045.jpg \\n http://www.xiuren.org/misc/okamoto/002/0046.jpg \\n http://www.xiuren.org/misc/okamoto/002/0047.jpg \\n http://www.xiuren.org/misc/okamoto/002/0048.jpg \\n http://www.xiuren.org/misc/okamoto/002/0049.jpg \\n http://www.xiuren.org/misc/okamoto/002/0050.jpg \\n http://www.xiuren.org/misc/okamoto/002/0051.jpg \\n http://www.xiuren.org/misc/okamoto/002/0052.jpg \\n http://www.xiuren.org/misc/okamoto/002/0053.jpg \\n http://www.xiuren.org/misc/okamoto/002/0054.jpg \\n http://www.xiuren.org/misc/okamoto/002/0055.jpg \\n http://www.xiuren.org/misc/okamoto/002/0056.jpg \\n http://www.xiuren.org/misc/okamoto/002/0057.jpg \\n http://www.xiuren.org/misc/okamoto/002/0058.jpg \\n http://www.xiuren.org/misc/okamoto/002/0059.jpg \\n http://www.xiuren.org/misc/okamoto/002/0060.jpg \\n http://www.xiuren.org/misc/okamoto/002/0061.jpg \\n http://www.xiuren.org/misc/okamoto/002/0062.jpg \\n http://www.xiuren.org/misc/okamoto/002/0063.jpg \\n http://www.xiuren.org/misc/okamoto/002/0064.jpg \\n http://www.xiuren.org/misc/okamoto/002/0065.jpg \\n http://www.xiuren.org/misc/okamoto/002/0066.jpg \\n http://www.xiuren.org/misc/okamoto/002/0067.jpg \\n http://www.xiuren.org/misc/okamoto/002/0068.jpg \\n http://www.xiuren.org/misc/okamoto/002/0069.jpg \\n http://www.xiuren.org/misc/okamoto/002/0070.jpg \\n http://www.xiuren.org/misc/okamoto/002/0071.jpg \\n http://www.xiuren.org/misc/okamoto/002/0072.jpg \\n http://www.xiuren.org/misc/okamoto/002/0073.jpg \\n http://www.xiuren.org/misc/okamoto/002/0074.jpg \\n http://www.xiuren.org/misc/okamoto/002/0075.jpg \\n http://www.xiuren.org/misc/okamoto/002/0076.jpg \\n http://www.xiuren.org/misc/okamoto/002/0077.jpg \\n http://www.xiuren.org/misc/okamoto/002/0078.jpg \\n http://www.xiuren.org/misc/okamoto/002/0079.jpg \\n http://www.xiuren.org/misc/okamoto/002/0080.jpg \\n http://www.xiuren.org/misc/okamoto/002/0081.jpg \\n http://www.xiuren.org/misc/okamoto/002/0082.jpg \\n http://www.xiuren.org/misc/okamoto/002/0083.jpg \\n http://www.xiuren.org/misc/okamoto/002/0084.jpg\"\n";
        String s = urls.replaceAll("(http.+?\\.jpg)", "<img src='$1'>");
        System.out.println(s);
    }

}
