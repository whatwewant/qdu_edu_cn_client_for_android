package Service;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by potter on 14-8-6.
 */
public class AnalyseLoginReturnResult {
    public static String LoginResult(String sourceHtml) {
        // 登录成功
        if (sourceHtml.indexOf("successfully") != -1)
            return "Logged in successfully.";

        // 用户不存在
        if (sourceHtml.indexOf("E63018") != -1)
            return "用户不存在或者用户没有申请该服务";

        // 在线用户数量限制
        if (sourceHtml.indexOf("E63022") != -1)
            return "在线用户数量限制";

        // 密码错误
        if (sourceHtml.indexOf("E63032") != -1) {
            /*
            String regex = "msga='E63032:([^\"]+)';";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(sourceHtml);
            return matcher.group().trim();*/
            return "E63032: 用户密码错误（如果您连续输入错误的密码10次后，将被加入黑名单，第二天才能解除，或者请联系管理员）";
        }

        String regex = "<td height=\"127\" width=\"99%\" align=\"center\">([^\"]+)<br>";
        regex = "msga='E63032: ([^\"]+)';";
        Pattern pattern = Pattern.compile(regex);

        Matcher matcher = pattern.matcher(sourceHtml);

        if ( matcher.find() ) {
            return matcher.group().replaceAll("<div style=\"padding-left: 10px;padding-right: 10px;\">|</div>", "").trim();
        }
        return "匹配失败";
    }

    public static String LogoutResult(String sourceHtml) {

        String regex = "<div style=\"padding-left: 10px;padding-right: 10px;\">([^\"]+)</div>";
        Pattern pattern = Pattern.compile(regex);

        Matcher matcher = pattern.matcher(sourceHtml);

        if ( matcher.find() ) {
            return matcher.group().replaceAll("<div style=\"padding-left: 10px;padding-right: 10px;\">|</div>", "").trim();
        }
        return "匹配失败";

    }

    public boolean HadLoginedByBaidu() throws IOException {
        // System.out.println("in HadLoginedByBaidu");
        MySimpleHttp client = new MySimpleHttp();
        String sourceHtml = client.get("http://www.baidu.com");
        if (sourceHtml.indexOf("baidu") != -1) {
            return true; // 已经登入
        }
        return false;
    }
}
