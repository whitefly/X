package spider.utils;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ReUtil {

    static Cache<String, Pattern> cache = CacheBuilder.newBuilder().maximumSize(500).build();

    final static String IMG_LINK_REGEX = "(https?:[^:<>\"]*\\/)([^:<>\"]*)(\\.(?:png|jpe?g|webp|gif))";
    final static String VIDEO_LINK_REGEX = "(https?:[^:<>\"]*\\/)([^:<>\"]*)(\\.m3u8)";

    public static List<String> regex(String re, String content, boolean all, boolean group1) {
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
                String group = group1 ? matcher.group(1) : matcher.group();
                result.add(group);
            }
        } else {
            if (matcher.find()) {
                result.add(group1 ? matcher.group(1) : matcher.group());
            }
        }
        return result;
    }

    public static void main(String[] args) {
        String content = "</a></p><p><img src=\"https://zzzttt18.com/usr/uploads/2020/11/1077751958.jpg\" alt=\"QQ截图20201130220844.jpg\" title=\"QQ截图20201130220844.jpg\"><br><img src=\"https://zzzttt18.com/usr/uploads/2020/11/2288550212.jpg\" alt=\"QQ截图20201130220916.jpg\" title=\"QQ截图20201130220916.jpg\"><br><img src=\"https://zzzttt18.com/usr/uploads/2020/11/187071483.jpg\" alt=\"QQ截图20201130220925.jpg\" title=\"QQ截图20201130220925.jpg\"><br><img src=\"https://zzzttt18.com/usr/uploads/2020/11/2083541212.jpg\" alt=\"QQ截图20201130220934.jpg\" title=\"QQ截图20201130220934.jpg\"><br><img src=\"https://zzzttt18.com/usr/uploads/2020/11/1082276667.jpg\" alt=\"QQ截图20201130220943.jpg\" title=\"QQ截图20201130220943.jpg\"><br><img src=\"https://zzzttt18.com/usr/uploads/2020/11/2363766288.jpg\" alt=\"QQ截图20201130220952.jpg\" title=\"QQ截图20201130220952.jpg\"><br><img src=\"https://zzzttt18.com/usr/uploads/2020/11/3774558551.jpg\" alt=\"QQ截图20201130221004.jpg\" title=\"QQ截图20201130221004.jpg\"><br><img src=\"https://zzzttt18.com/usr/uploads/2020/11/1173557385.jpg\" alt=\"QQ截图20201130221013.jpg\" title=\"QQ截图20201130221013.jpg\"><br><img src=\"https://zzzttt18.com/usr/uploads/2020/11/1870924165.jpg\" alt=\"QQ截图20201130221022.jpg\" title=\"QQ截图20201130221022.jpg\"><br><img src=\"https://zzzttt18.com/usr/uploads/2020/11/979416138.jpg\" alt=\"QQ截图20201130221043.jpg\" title=\"QQ截图20201130221043.jpg\"><br><img src=\"https://zzzttt18.com/usr/uploads/2020/11/3268967722.jpg\" alt=\"QQ截图20201130221104.jpg\" title=\"QQ截图20201130221104.jpg\"></p><p><div class=\"dplayer\" data-config='{\"live\":false,\"autoplay\":false,\"theme\":\"#FADFA3\",\"loop\":false,\"screenshot\":false,\"hotkey\":true,\"preload\":\"none\",\"lang\":\"zh-cn\",\"logo\":null,\"volume\":0.7,\"mutex\":true,\"video\":{\"url\":\"https:\\/\\/v.muyuanwy.com\\/public\\/videos\\/5fc4fbe319a1697166f36399\\/index.m3u8\",\"pic\":\"\",\"type\":\"auto\",\"thumbnails\":null}}'></div></p><p><div class=\"dplayer\" data-config='{\"live\":false,\"autoplay\":false,\"theme\":\"#FADFA3\",\"loop\":false,\"screenshot\":false,\"hotkey\":true,\"preload\":\"none\",\"lang\":\"zh-cn\",\"logo\":null,\"volume\":0.7,\"mutex\":true,\"video\":{\"url\":\"https:\\/\\/rd.91cdn.xyz\\/hls\\/videos\\/30000\\/30390\\/30390.mp4\\/index.m3u8?sid=\",\"pic\":\"\",\"type\":\"auto\",\"thumbnails\":null}}'></div></p><p>高清完整版：<a href=\"https://zzzttt.life/xo\">https://zzzttt.life/xo</a></p><hr><p>更多福利APP：<a href=\"https://zzzttt.life/666\" target=\"_blank\" class=\"btn btn-primary\">点击进入</a></p> </div>\n" +
                "<div class=\"tags\">\n" +
                "<div itemprop=\"keywords\" class=\"keywords ainAbh\"></div>\n" +
                "</div>\n" +
                "<style type=\"text/css\">\n" +
                "            .flash {\n" +
                "                font-weight: bold;\n" +
                "                font-size: calc(10px + 4vh);\n" +
                "                line-height: calc(10px + 6.6vh);";

    }

}
