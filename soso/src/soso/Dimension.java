package soso;

/**
 * Created by zhoujia on 2017/6/17.
 */
public enum Dimension {
    MIN_1(0, "1分钟"),
    MIN_3(7, "3分钟"),
    MIN_5(1, "5分钟"),
    MIN_10(5, "10分钟"),
    MIN_15(2, "15分钟"),
    MIN_30(9, "30分钟"),
    HOUR_1(10, "1小时"),
    HOUR_2(11, "2小时"),
    HOUR_4(12, "4小时"),
    HOUR_6(13, "6小时"),
    HOUR_12(14, "12小时");

    private String name;
    private int type;

    private Dimension(int type, String name) {
        this.type = type;
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public String getName() {
        return name;
    }
}
