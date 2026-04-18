package com.example.izakayakaikei;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtil {

    // 営業日の日付キーを返す（午前6時前は前日扱い）
    public static String getBusinessDateKey() {
        Calendar cal = Calendar.getInstance();
        if (cal.get(Calendar.HOUR_OF_DAY) < 6) {
            cal.add(Calendar.DATE, -1);
        }
        return new SimpleDateFormat("yyyy-MM-dd", Locale.JAPAN).format(cal.getTime());
    }

    // 現在の西暦・月キーを返す（午前6時ルール適用）
    public static String getBusinessYearMonth() {
        Calendar cal = Calendar.getInstance();
        if (cal.get(Calendar.HOUR_OF_DAY) < 6) {
            cal.add(Calendar.DATE, -1);
        }
        return new SimpleDateFormat("yyyy年MM月", Locale.JAPAN).format(cal.getTime());
    }

    // 表示用の日付文字列（例: 4/19 23:30）
    public static String getDisplayDateTime() {
        return new SimpleDateFormat("M/d HH:mm", Locale.JAPAN).format(new Date());
    }

    // dateKeyから表示用の日付を返す（例: 4月19日）
    public static String formatDateKeyToDisplay(String dateKey) {
        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.JAPAN).parse(dateKey);
            return new SimpleDateFormat("M月d日", Locale.JAPAN).format(date);
        } catch (Exception e) {
            return dateKey;
        }
    }

    // dateKeyの月を返す（例: "2026年04月"）
    public static String getYearMonthFromKey(String dateKey) {
        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.JAPAN).parse(dateKey);
            return new SimpleDateFormat("yyyy年MM月", Locale.JAPAN).format(date);
        } catch (Exception e) {
            return "";
        }
    }
}
