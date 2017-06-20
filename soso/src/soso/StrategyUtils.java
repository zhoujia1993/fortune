package soso;

import soso.model.KLine;

/**
 * Created by zhoujia on 2017/6/17.
 */
public class StrategyUtils {

    public static double[] getCloseEMA(KLine kLine, int weight, int cycle) {
        if (kLine == null || kLine.datas == null || kLine.datas.size() == 0) {
            return null;
        }
        double[] ema = new double[kLine.datas.size()];
        ema[0] = kLine.datas.get(0).close;

        for (int i = 1; i < kLine.datas.size(); i++) {
            ema[i] = weight * kLine.datas.get(i).close / (cycle + 1) + (cycle + 1 - weight) * ema[i - 1] / (cycle + 1);
        }
        return ema;
    }

    public static double[] getEMA(double[] data, int weight, int cycle) {
        if (data == null || data.length == 0) {
            return null;
        }
        double[] ema = new double[data.length];
        ema[0] = data[0];

        for (int i = 1; i < data.length; i++) {
            ema[i] = weight * data[i] / (cycle + 1) + (cycle + 1 - weight) * ema[i - 1] / (cycle + 1);
        }
        return ema;
    }


}
