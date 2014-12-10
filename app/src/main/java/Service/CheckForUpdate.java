package Service;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by potter on 14-12-10.
 */
public class CheckForUpdate {
    private static String APK_URL = "https://github.com/whatwewant/qdu_edu_cn_client_for_android/raw/master/app/app.apk";
    private static String VERSION_URL = "https://raw.githubusercontent.com/whatwewant/qdu_edu_cn_client_for_android/master/app/src/main/java/Service/CheckForUpdate.java";

    public static int big = 1;
    public static int release = 0;
    public static int bug = 6;

    public static String QDU_EDU_CN_VERSION = "1.0.6";

    public static String get_newest_version() {
        try {
            String httpResult = MySimpleHttp.get_static(VERSION_URL);
            String current_reg = "public static String QDU_EDU_CN_VERSION = \"(.+)\";";
            String regexResult = MyRegularExpresion.regexHtml(httpResult, current_reg);
            if (regexResult == null)
                return "不存在";
            return regexResult.replaceAll("public static String QDU_EDU_CN_VERSION = \"", "")
                              .replaceAll("\";", "");
        } catch (IOException e) {
            return "奔溃了";
        }
    }

    public static String check() {
        String newVersion = get_newest_version();

        try {
            int bigRelease = Integer.parseInt(newVersion.split("\\.")[0]);
            int releaseNum = Integer.parseInt(newVersion.split("\\.")[1]);
            int smallBug = Integer.parseInt(newVersion.split("\\.")[2]);

            if (big > bigRelease ||
                    (bigRelease==big && releaseNum>release) ||
                    (big==bigRelease && release==releaseNum && smallBug>bug)) {
                return ("检测到新版本: Version " + newVersion);
            }
            return "已是最新版本Version: " + QDU_EDU_CN_VERSION + ", 无需更新";
        }
        catch (Exception e) {
            return "检测失败";
        }
    }

    public static File update() {
        final String fileName = "qdu_edu_cn_update.apk";
        File tmpFile = new File("/sdcard/update");
        if (!tmpFile.exists()) {
            tmpFile.mkdir();
        }
        final File file = new File("/sdcard/update/" + fileName);

        try {
            URL url = new URL(APK_URL);
            try {
                HttpURLConnection conn = (HttpURLConnection) url
                        .openConnection();
                InputStream is = conn.getInputStream();
                FileOutputStream fos = new FileOutputStream(file);
                byte[] buf = new byte[256];
                conn.connect();
                double count = 0;
                if (conn.getResponseCode() >= 400) {
                    return null;
                } else {
                    while (count <= 100) {
                        if (is != null) {
                            int numRead = is.read(buf);
                            if (numRead <= 0) {
                                break;
                            } else {
                                fos.write(buf, 0, numRead);
                            }

                        } else {
                            break;
                        }

                    }
                }

                conn.disconnect();
                fos.close();
                is.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block

                e.printStackTrace();
            }
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block

            e.printStackTrace();
        }

        return file;
    }


}
