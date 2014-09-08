package Service;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by potter on 14-7-31.
 */
public class ReachIP {
    public static boolean isIpReachable(String ip) throws IOException {
        InetAddress addr = InetAddress.getByName(ip);
        System.out.println("in ReachIp: reach " + ip + ": " + addr.isReachable(3000));
        if (addr.isReachable(3000))
        {
            return true;
        }
        return false;
    }
}
