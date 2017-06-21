package test;

import soso.Coin;
import soso.CoinInfo;
import soso.DataCenter;
import soso.Dimension;
import soso.strategydeal.StrategyDealWrapper;
import strategy.SmartGDFStrategy;
import trade.SmartGDFTrade;
import yunbi.YunBi;

/**
 * Created by zhoujia on 2017/6/20.
 */
public class Main {
    /**
     * 使用前在yunbi.Config中注册access_key和secret_key
     * @param args
     */
    public static void main(String[] args) {
        CoinInfo coinInfo = new CoinInfo("cn", "yunbi", "sc", "yunbi_sc", Dimension.MIN_1.getType());
        Coin.getInstance().registerStrategy(coinInfo, new StrategyDealWrapper(new SmartGDFStrategy("sc模拟" + Dimension.MIN_1.getName(), 0.002), new SmartGDFTrade("sccny")));
        YunBi.getInstance().init();
        new DataCenter().start();
    }
}
