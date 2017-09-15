package strategy;

import soso.CoinInfo;
import soso.Dimension;
import soso.model.DepthInfo;
import soso.model.KLine;
import soso.model.TradesInfo;
import soso.strategydeal.DealHandle;
import soso.strategydeal.Strategy;

import java.util.List;

/**
 * Created by zhoujia on 2017/7/15.
 */
public class CandlestickChartsStrategy implements Strategy {
    @Override
    public boolean onHandle(CoinInfo coinInfo, Dimension type, KLine kLine, TradesInfo tradesInfo, DepthInfo depthInfo, DealHandle dealHandle) {
        return false;
    }

    @Override
    public boolean onTestHandle(CoinInfo coinInfo, Dimension type, KLine kLine, TradesInfo tradesInfo, DepthInfo depthInfo) {
        List<KLine.Data> datas = kLine.datas;
        double price = 0;
        for (KLine.Data data : datas) {
            price += data.close;
        }
        double avg = price / datas.size();
        System.out.println("当日均价=" + avg + "可交易的上下幅度=" + (avg * 0.95) + "~" + (avg * 1.05));
        return false;
    }
}
