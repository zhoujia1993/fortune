package strategy;

import soso.CoinInfo;
import soso.StrategyUtils;
import soso.model.DepthInfo;
import soso.model.KLine;
import soso.model.TradesInfo;
import soso.strategydeal.DealHandle;
import soso.strategydeal.Strategy;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * 金叉买，死叉卖的策略，基本无用，参开参考吧
 * Created by zhoujia on 2017/6/17.
 */

public class GDFStrategy implements Strategy {
    public String name;

    public int amount;
    public double money = 0;
    private boolean buy = false;
    private double payback;

    private double RATE = 0.998001;

    public GDFStrategy(String name) {
        this.name = name;
    }

    @Override
    public boolean onTestHandle(CoinInfo coinInfo, int type, KLine kLine, TradesInfo tradesInfo, DepthInfo depthInfo) {
//            System.out.println(kLine.toString());
//            System.out.println(tradesInfo.toString());
//            System.out.println(depthInfo.toString());
        DecimalFormat df = new DecimalFormat(".00000");
        List<KLine.Data> kLineData = kLine.datas;
        double[] ema12 = StrategyUtils.getCloseEMA(kLine, 2, 7);
//            System.out.println(Arrays.toString(ema12));
        double[] ema26 = StrategyUtils.getCloseEMA(kLine, 2, 30);
//            System.out.println(Arrays.toString(ema26));
        for (int i = 1; i < ema12.length - 1; i++) {
            double e12 = ema12[i];
            double e26 = ema26[i];
            double e12before = ema12[i - 1];
            double e26before = ema26[i - 1];
            double e12after = ema12[i + 1];
            double e26after = ema26[i + 1];
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            if (e12before < e26before && e12 > e26) {
//                    System.out.println("金叉=" + sdf.format(kLineData.get(i).date * 1000) + "  收盘价=" + kLineData.get(i).close);
                money = kLineData.get(i).close;
                buy = true;
            }
            if (e12 > e26 && e12after < e26after && buy) {
//                    System.out.println("死叉=" + sdf.format(kLineData.get(i + 1).date * 1000) + "  收盘价=" + kLineData.get(i + 1).close);
                double sell = money / 0.998001;
                payback += kLineData.get(i + 1).close - sell;
//                    if (sell > kLineData.get(i).close) {
//                    System.out.println("成本价=" + sell + "   收盘价=" + kLineData.get(i + 1).close + "  净赚=" + df.format(kLineData.get(i + 1).close - sell));
                buy = false;
            }

        }

        System.out.println(name + "收入=" + df.format(payback));
        return false;
    }

    @Override
    public boolean onHandle(CoinInfo coinInfo, int type, KLine kLine, TradesInfo tradesInfo, DepthInfo depthInfo, DealHandle dealHandle) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<KLine.Data> kLineData = kLine.datas;
        double[] ema7 = StrategyUtils.getCloseEMA(kLine, 2, 7);
        double[] ema30 = StrategyUtils.getCloseEMA(kLine, 2, 30);
        int ema7Length = ema7.length;
        int ema30Length = ema30.length;
        double nowEma7 = ema7[ema7Length - 1];
        double nowEma30 = ema30[ema30Length - 1];
        double beforeEma7 = ema7[ema7Length - 2];
        double beforeEma30 = ema30[ema30Length - 2];
        KLine.Data data = kLineData.get(kLineData.size() - 1);
        boolean buy = false;
        if (nowEma7 > nowEma30 && beforeEma7 < beforeEma30) {
            if (!buy) {
                System.out.println("金叉=" + sdf.format(data.date * 1000) + "  " + data.toString());
                buy = true;
            }
        } else if (nowEma7 < nowEma30 && beforeEma7 > beforeEma30) {
            if (buy) {
                System.out.println("死叉=" + sdf.format(data.date * 1000) + "  " + data.toString());
                buy = false;
            }
        } else {
            System.out.println("正常波动" + sdf.format(data.date * 1000) + "  " + data.toString());
        }


        return true;
    }
}