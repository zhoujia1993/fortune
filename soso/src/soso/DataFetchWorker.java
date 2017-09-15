package soso;

import soso.model.DepthInfo;
import soso.model.KLine;
import soso.model.TradesInfo;
import soso.net.JsonParam;
import soso.net.Method;
import soso.net.NetService;
import soso.strategydeal.StrategyDealWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;


/**
 * Created by zhoujia on 2017/6/17.
 */
public class DataFetchWorker implements Runnable {
    private CountDownLatch downLatch;
    private NetService<DepthInfo> coinDepthService;
    private JsonParam coinDepthTradesParams;
    private List<CoinInfo.CoinKLine> coinKLineParams;
    private NetService<TradesInfo> coinTradesService;
    private NetService<KLine> coinKLineService;
    private List<StrategyDealWrapper> strategyDealWrappers;
    private CoinInfo coinInfo;

    public DataFetchWorker(CoinInfo coinInfo, List<StrategyDealWrapper> strategies, CountDownLatch downLatch) {
        this.coinInfo = coinInfo;
        this.strategyDealWrappers = strategies;
        this.coinDepthTradesParams = new JsonParam();
        this.coinDepthTradesParams.put(coinInfo.getCoinDepthTrades());
        this.coinKLineParams = new ArrayList<>();
        for (CoinInfo.CoinKLine kLine : coinInfo.getCoinKLine()) {
            this.coinKLineParams.add(kLine);
        }

        this.coinTradesService = new NetService<TradesInfo>();
        this.coinKLineService = new NetService<KLine>();
        this.coinDepthService = new NetService<DepthInfo>();
        this.downLatch = downLatch;
    }


    @Override
    public void run() {
        synchronized (DataFetchWorker.class) {
            for(int i=0;i<coinKLineParams.size();i++){
                CoinInfo.CoinKLine coinKLine = coinKLineParams.get(i);
                JsonParam klineParam = new JsonParam();
                klineParam.put(coinKLine);
                if (!Config.DEBUG) {
                    TradesInfo tradesInfo = coinTradesService.request(Method.POST, Config.GET_TRADES_URL, coinDepthTradesParams, TradesInfo.class);
                    KLine kLine = coinKLineService.request(Method.POST, Config.GET_KLINE_URL, klineParam, KLine.class);
                    if (kLine != null) {
                        kLine.getKLine();
                    }
                    DepthInfo depthInfo = coinDepthService.request(Method.POST, Config.GET_DEPTHINFO_URL, coinDepthTradesParams, DepthInfo.class);
                    if (depthInfo != null) {
                        depthInfo.getDepthInfo();
                    }
                    for (StrategyDealWrapper strategyDealWrapper : strategyDealWrappers) {
                        if (strategyDealWrapper != null && strategyDealWrapper.getStrategy() != null) {
                            if (strategyDealWrapper.getStrategy().onHandle(coinInfo, coinInfo.type[i], kLine, tradesInfo, depthInfo, strategyDealWrapper.getDealHandle())) {
                                break;
                            }
                        }
                    }
                } else {
                    //暂时还是拿网络数据，以后考虑从数据库拿数据
                    TradesInfo tradesInfo = coinTradesService.request(Method.POST, Config.GET_TRADES_URL, coinDepthTradesParams, TradesInfo.class);
                    KLine kLine = coinKLineService.request(Method.POST, Config.GET_KLINE_URL, klineParam, KLine.class).getKLine();
                    DepthInfo depthInfo = coinDepthService.request(Method.POST, Config.GET_DEPTHINFO_URL, coinDepthTradesParams, DepthInfo.class).getDepthInfo();
                    for (StrategyDealWrapper strategyDealWrapper : strategyDealWrappers) {
                        if (strategyDealWrapper != null && strategyDealWrapper.getStrategy() != null) {
                            if (strategyDealWrapper.getStrategy().onTestHandle(coinInfo, coinInfo.type[i],kLine, tradesInfo, depthInfo)) {
                                break;
                            }
                        }
                    }
                }
            }
            downLatch.countDown();
        }
    }
}
