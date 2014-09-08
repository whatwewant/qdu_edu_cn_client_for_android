package Service;

import android.provider.Telephony;

import java.io.IOException;

/**
 * Created by potter on 14-7-31.
 */
public class Ping {
    private static String AddrIP = "www.baidu.com";

    public static boolean ping() throws InterruptedException, IOException {
        Process p = Runtime.getRuntime().exec("ping -c 3 -w 100" + AddrIP);
        int status = p.waitFor();

        System.out.println("In Ping: " + status);

        if (status == 0)
            return true;
        return false;
    }
}
