package Service;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by potter on 14-8-6.
 */
public class TimeSet {
    public static int hour = 0;
    public static int minute = 0;
    public static int second = 0;

    // 获取系统时间
    public static String setTime() {
        Calendar tmp = Calendar.getInstance();
        hour = tmp.get(Calendar.HOUR_OF_DAY);
        minute = tmp.get(Calendar.MINUTE);
        second = tmp.get(Calendar.SECOND);

        return (hour<10?"0"+String.valueOf(hour):hour) + ":" + (minute<10?"0"+String.valueOf(minute):minute) + ":" + (second<10?"0"+String.valueOf(second):second);
    }

    // 计时
    public static String takeTime() {
        second += 1;

        if (second >= 60) {
            second = 0;
            minute += 1;
            if (minute >= 60) {
                hour += 1;
                if (hour >= 99) {
                    hour = 0;
                }
            }
        }

        return (hour<10?"0"+String.valueOf(hour):hour) + ":" + (minute<10?"0"+String.valueOf(minute):minute) + ":" + (second<10?"0"+String.valueOf(second):second);
    }
}
