package soso;

import java.util.Map;

/**
 * Created by zhoujia on 2017/6/16.
 */
public class Utils {
    public static boolean isEmpty(String text) {
        return text == null || text.length() == 0;
    }

    public static boolean isCollectionsEmpty(Map map) {
        return map == null || map.size() == 0;
    }
}
