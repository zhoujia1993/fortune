package soso;

/**
 * Created by zhoujia on 2017/6/17.
 */
public class Config {

    /**
     * 生产环境还是测试环境，手动设置
     */
    public static boolean DEBUG = false;
    public static final String WECHAT_APPID = "wx852228bab8170c2f";
    public static final String WECHAT_SECRET_KEY = "c1f2b52e9affef6e64f2b136dbc5b29a";
    public static final String WECHAT_TO_USER = "oRA_lwpJGcUh5QqGLUZRTvmUgTGc";
    public static final String WECHAT_TEMPLATE1 = "vIQPMVvaNdo0UH66KuFXq051HQRR9HndAw3rAmG-a7M";
    public static final String WECHAT_TEMPLATE2 = "6ARNQ__6th_71FwxSaO2XfBavaaYqpMjQq6u6xKo-M0";
    public static final String WECHAT_TEMPLATE3 = "eaMudg3eyNMgHiJSFEfVirRt1U7vz4PuGPhcug4aEGM";

    public static final String GET_TRADES_URL = "https://api.sosobtc.com/direct/android/gettrades.php";
    public static final String GET_KLINE_URL = "https://api.sosobtc.com/direct/v2/kline";
    public static final String GET_DEPTHINFO_URL = "https://api.sosobtc.com/direct/android/depthinfo.php";
}
