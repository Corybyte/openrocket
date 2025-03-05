package net.sf.openrocket.utils;

import java.util.Collections;
import java.util.Comparator;

public class eduUTIL {

    public static net.sf.openrocket.util.ArrayList<String> sortByTimestamp(net.sf.openrocket.util.ArrayList<String> list) {
        // 使用自定义比较器对列表进行排序
        Collections.sort(list, new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                // 提取 s1 的时间戳
                double timestamp1 = extractTimestamp(s1);
                // 提取 s2 的时间戳
                double timestamp2 = extractTimestamp(s2);
                // 比较时间戳
                return Double.compare(timestamp1, timestamp2);
            }
        });

        return list;
    }

    private static double extractTimestamp(String s) {
        // 找到时间戳结束的位置（即 "]")
        int endIndex = s.indexOf("]");
        if (endIndex != -1) {
            // 提取时间戳部分并转换为 long
            return Double.parseDouble(s.substring(1, endIndex));
        }
        // 如果格式不正确，返回 0（可以根据需求调整）
        return 0;
    }
}
