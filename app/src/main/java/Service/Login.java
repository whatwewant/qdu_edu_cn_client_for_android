package Service;

import android.util.Log;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by potter on 14-7-30.
 */
public class Login {

    private final static String calg = "12345678";
    private static String pid = "1";
    private static String R1 = "0";
    private static String R2 = "1";
    private static String para = "00";
    private static String _0MKKey = "123456";

    public static String loginPortal(String url, String username, String password) throws IOException {
        String result = null;
        // Login 前 检查是否已登入
        result = (new MySimpleHttp()).get("http://10.0.109.2");
        if (result.indexOf("wc") != -1)
            return "已经登录";

        String md5Password = MD5.md5(pid + password + calg) + calg + pid;
        Log.d("md5Password", md5Password);
        ArrayList<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair("DDDDD", username));
        nvps.add(new BasicNameValuePair("upass", md5Password));
        nvps.add(new BasicNameValuePair("R1", R1));
        nvps.add(new BasicNameValuePair("R2", R2));
        nvps.add(new BasicNameValuePair("para", para));
        nvps.add(new BasicNameValuePair("0MKKey", _0MKKey));

        try {
            result = new MySimpleHttp().post(url, nvps);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (result == null)
            return "联网失败";

        return AnalyseLoginReturnResult.LoginResult(result);
    }
}
