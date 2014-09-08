package Service;

import android.util.Base64;


/**
 * Created by potter on 14-7-31.
 */

public class B64encoder {
    public static String encryptBASE64(String key) {
        byte [] b = key.getBytes();
        return Base64.encodeToString(b, Base64.NO_WRAP);
    }
}
