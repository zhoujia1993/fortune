package soso;

/**
 * Created by zhoujia on 2017/6/17.
 */
public class Config {
    /**
     * 生产环境还是测试环境，手动设置
     */
    public static final boolean DEBUG = false;

    public static final String GET_TRADES_URL = "https://api.sosobtc.com/direct/android/gettrades.php";
    public static final String GET_KLINE_URL = "https://api.sosobtc.com/direct/v2/kline";
    public static final String GET_DEPTHINFO_URL = "https://api.sosobtc.com/direct/android/depthinfo.php";
}
