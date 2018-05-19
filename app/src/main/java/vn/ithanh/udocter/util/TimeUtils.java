package vn.ithanh.udocter.util;
import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by iThanh on 1/4/2018.
 */

public class TimeUtils {
    public final static long ONE_SECOND = 1;
    public final static long ONE_MINUTE = ONE_SECOND * 60;
    public final static long ONE_HOUR = ONE_MINUTE * 60;
    public final static long ONE_DAY = ONE_HOUR * 24;
    /**
     * converts time (in milliseconds) to human-readable format
     *  "<w> days, <x> hours, <y> minutes and (z) seconds"
     */
    public static String millisToLongDHMS(String sDate) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date d1 = new Date();
        Date d2 = formatter.parse(sDate);
        long diff = d1.getTime() - d2.getTime();//as given
        return friendlyTimeDiff(diff);
    }

    public static String friendlyTimeDiff(long timeDifferenceMilliseconds) {
        long diffSeconds = timeDifferenceMilliseconds / 1000;
        long diffMinutes = timeDifferenceMilliseconds / (60 * 1000);
        long diffHours = timeDifferenceMilliseconds / (60 * 60 * 1000);
        long diffDays = timeDifferenceMilliseconds / (60 * 60 * 1000 * 24);
        long diffWeeks = timeDifferenceMilliseconds / (60 * 60 * 1000 * 24 * 7);
        long diffMonths = (long) (timeDifferenceMilliseconds / (60 * 60 * 1000 * 24 * 30.41666666));
        long diffYears = timeDifferenceMilliseconds / ((long)60 * 60 * 1000 * 24 * 365);
//        Log.i("TAG", "-------------------------------------------------: ");
//        Log.i("diffSeconds : ", diffSeconds+"");
//        Log.i("diffMinutes : ", diffMinutes+"");
//        Log.i("diffHours : ", diffHours+"");
//        Log.i("diffDays : ", diffDays+"");
//        Log.i("diffWeeks : ", diffWeeks+"");
//        Log.i("diffMonths : ", diffMonths+"");
//        Log.i("diffYears : ", diffYears+"");
//        Log.i("TAG", "-------------------------------------------------: ");
//

        if (diffSeconds < 1) {
            return "vừa xong";
        } else if (diffMinutes < 1) {
            return diffSeconds + " giây trước";
        } else if (diffHours < 1) {
            return diffMinutes + " phút trước";
        } else if (diffDays < 1) {
            return diffHours + " giờ trước";
        } else if (diffWeeks < 1) {
            return diffDays + " ngày trước";
        } else if (diffMonths < 1) {
            return diffWeeks + " tuần trước";
        } else if (diffYears < 1) {
            return diffMonths + " tháng trước";
        } else {
            return diffYears + " năm trước";
        }
    }
    public static int compareTimes(Date d1, Date d2)
    {
        int     t1;
        int     t2;
        t1 = (int) (d1.getTime() % (24*60*60*1000L));
        t2 = (int) (d2.getTime() % (24*60*60*1000L));
        return (t1 - t2);
    }
//    public static long compareTimes(Date d1, Date d2)
//    {
//        long     t1;
//        long     t2;
//        t1 = d1.getHours()*60*60+d1.getMinutes()*60+d1.getSeconds();
//        t2 = d2.getHours()*60*60+d2.getMinutes()*60+d2.getSeconds();
//        long diff = t1-t2;
//        System.out.println("t1 - "+t1+", t2 - "+t2+",  diff - "+diff);
//
//        return diff;
//    }
}
