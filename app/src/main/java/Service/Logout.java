package Service;

import java.io.IOException;

/**
 * Created by potter on 14-9-8.
 */
public class Logout {
    public static String logout() {
        String result = null;
        try {
            result = new MySimpleHttp().get("http://10.0.109.2/F.htm");
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (result == null)
            return "注销失败";

        if (result.indexOf("modify") != -1)
            return "注销成功";

        System.out.println(result);

        return AnalyseLoginReturnResult.LogoutResult(result);
    }
}
