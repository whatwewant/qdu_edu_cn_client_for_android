package Service;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by potter on 14-7-31.
 */
public class MyNetworkInfo {
    /**
     * 测试ConnectivityManager
     * ConnectivityManager主要管理和网络连接相关的操作
     * 相关的TelephonyManager则管理和手机、运营商等的相关信息；WifiManager则管理和wifi相关的信息。
     * 想访问网络状态，首先得添加权限<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
     * NetworkInfo类包含了对wifi和mobile两种网络模式连接的详细描述,通过其getState()方法获取的State对象则代表着
     * 连接成功与否等状态。
     *
     */
    private ConnectivityManager connManager;

    // 是否启用网络
    public boolean networkOn (Context context) {
        // context = getApplocationContext();
        connManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connManager.getActiveNetworkInfo();

        //  未开启网络
        if (networkInfo == null) {
            return false;
        }

        // Wifi网络
        NetworkInfo.State state = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
        if(NetworkInfo.State.CONNECTED != state)
            return false;

        return true;
    }
}
