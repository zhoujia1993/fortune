package test;

import soso.Coin;
import soso.CoinInfo;
import soso.DataCenter;
import soso.Dimension;
import soso.strategydeal.StrategyDealWrapper;
import yunbi.YunBi;

/**
 * Created by zhoujia on 2017/6/20.
 */
public class Main {
    /**
     * 使用前在yunbi.Config中注册access_key和secret_key
     *
     * @param args
     */
    public static void main(String[] args) {

//        Config.DEBUG = true;
        Dimension[] dimensions = new Dimension[]{Dimension.MIN_1, Dimension.MIN_15};
        CoinInfo eos = new CoinInfo("cn", "yunbi", "eos", "yunbi_eos", dimensions);
        CoinInfo gxs = new CoinInfo("cn", "yunbi", "gxs", "yunbi_gxs", dimensions);
        CoinInfo sc = new CoinInfo("cn", "yunbi", "sc", "yunbi_sc", dimensions);
        CoinInfo bts = new CoinInfo("cn", "yunbi", "bts", "yunbi_bts", dimensions);
        CoinInfo ost = new CoinInfo("cn", "yunbi", "1st", "yunbi_1st", dimensions);
        CoinInfo snt = new CoinInfo("cn", "yunbi", "snt", "yunbi_snt", dimensions);


        BollStrategy eosStrategy = new BollStrategy("eoscny", "EOS", 0.01,0.02);
        BollStrategy gxsStrategy = new BollStrategy("gxscny", "GXS", 0.001,0.02);
        BollStrategy scStrategy = new BollStrategy("sccny", "SC", 0.00001,0.02);
        BollStrategy btsStrategy = new BollStrategy("btscny", "BTS", 0.0001,0.02);
        BollStrategy ostStrategy = new BollStrategy("1stcny", "1ST", 0.0001,0.02);
        BollStrategy sntStrategy = new BollStrategy("sntcny", "SNT", 0.001, 0.02);

        Coin.getInstance().registerStrategy(eos, new StrategyDealWrapper(eosStrategy, null));
        Coin.getInstance().registerStrategy(gxs, new StrategyDealWrapper(gxsStrategy, null));
        Coin.getInstance().registerStrategy(sc, new StrategyDealWrapper(scStrategy, null));
        Coin.getInstance().registerStrategy(bts, new StrategyDealWrapper(btsStrategy, null));
        Coin.getInstance().registerStrategy(ost, new StrategyDealWrapper(ostStrategy, null));
        Coin.getInstance().registerStrategy(snt, new StrategyDealWrapper(sntStrategy, null));

//        CoinInfo coinInfo = new CoinInfo("cn", "yunbi", "sc", "yunbi_sc", new int[]{Dimension.MIN_1.getType()});
//        Coin.getInstance().registerStrategy(coinInfo, new StrategyDealWrapper(new SmartGDFStrategy("sc模拟" + Dimension.MIN_1.getName(), 0.002), new SmartGDFTrade("sccny")));

//        TradeInfoStrategy tradeInfoStrategy = new TradeInfoStrategy(10000, 10000, 0.01, 5, 0.00, 0.01, 0.0041, 2,180,10);
//        Coin.getInstance().registerStrategy(coinInfo, new StrategyDealWrapper(tradeInfoStrategy,  new SmartGDFTrade("sccny")));


//        Coin.getInstance().registerStrategy(gxs, new StrategyDealWrapper(gxsStrategy, null));
//        CandlestickChartsStrategy candlestickChartsStrategy = new CandlestickChartsStrategy();
//        Coin.getInstance().registerStrategy(coinInfo, new StrategyDealWrapper(candlestickChartsStrategy, null));

        YunBi.getInstance().init();
        new DataCenter().start();
//        System.out.println(String.valueOf(Double.valueOf("1592") * 0.999));
    }
}
