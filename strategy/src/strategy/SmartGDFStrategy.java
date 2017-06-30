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

/**基于金叉买，死叉卖的千分之二收益策略，实际收益情况有待验证。
 * Created by zhoujia on 2017/6/18.
 */
public class SmartGDFStrategy implements Strategy {

    public String name;

    public double money = 0;
    private boolean buy = false;
    private boolean sellFlag = false;
    private double payback;

    private double RATE = 0.998001;

    private double flag;

    private int id = 0;

    public SmartGDFStrategy(String name, double threshold) {
        this.name = name;
        payback = 0;
        flag = (1 + threshold)/RATE;
    }

    @Override
    public boolean onHandle(CoinInfo coinInfo,int type, KLine kLine, TradesInfo tradesInfo, DepthInfo depthInfo, DealHandle dealHandle) {
        if (kLine == null) {
            return false;
        }
        DecimalFormat df = new DecimalFormat(".00000");
        List<KLine.Data> kLineData = kLine.datas;
        double[] ema12 = StrategyUtils.getCloseEMA(kLine, 2, 7);
//            System.out.println(Arrays.toString(ema12));
        double[] ema26 = StrategyUtils.getCloseEMA(kLine, 2, 30);
//            System.out.println(Arrays.toString(ema26));
        int i = kLineData.size() - 1;
        double e12 = ema12[i];
        double e26 = ema26[i];
        double e12before = ema12[i - 1];
        double e26before = ema26[i - 1];
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        if (e12before < e26before && e12 > e26&&!buy) {
            sellFlag = false;
            System.out.println(name + "金叉=" + sdf.format(kLineData.get(i).date * 1000) + "  收盘价=" + kLineData.get(i).close);
            money = kLineData.get(i).low + (kLineData.get(i).high - kLineData.get(i).low) * 0.1;
            System.out.println("购买价格=" + money);
            if (dealHandle != null && !buy) {
                buy = true;
                dealHandle.onDealBuy(coinInfo, id, "0", df.format(money), String.valueOf(kLine.datas.get(kLine.datas.size() - 1).close));

            }
        } else if (e12before > e26before && e12 < e26 && buy) {
            buy = false;
            KLine.Data data = kLineData.get(i);
            double price = data.low + (data.high - data.low) * 0.8;
            System.out.println(name + "死叉=" + sdf.format(kLineData.get(i).date * 1000) + "  收盘价=" + price);
            double sell = money / 0.998001;
            payback += Double.valueOf(df.format(price - sell));
//                    if (sell > kLineData.get(i).close) {
            System.out.println(name + "成本价=" + sell + "   收盘价=" + price + "  净赚=" + df.format(price - sell));
            if (dealHandle != null) {
                int state = dealHandle.onDealSellQuery(coinInfo, id);
                if (state == DealHandle.STATE_WAIT) {
//                    dealHandle.onDealSellCancel(coinInfo, id, String.valueOf(0), "");
                    dealHandle.onDealSell(coinInfo, id, "0", df.format(price),String.valueOf(kLine.datas.get(kLine.datas.size() - 1).close));
                } else if (state == dealHandle.STATE_UNKNOWN) {
                    dealHandle.onDealSell(coinInfo, id, "0", df.format(price),String.valueOf(kLine.datas.get(kLine.datas.size() - 1).close));
                }
            }
            id++;
        } else if (buy && !sellFlag) {
            KLine.Data nowData = kLineData.get(i);
            KLine.Data beforeData = kLineData.get(i - 1);
            if (nowData.close < beforeData.close) {
                if (nowData.close >= money * flag) {
                    double temp = nowData.low + (nowData.high - nowData.low) * 0.8;
                    double price = temp >= money * flag ? temp : nowData.close;
                    double nowAmount = nowData.amount;
                    double beforeAmount = beforeData.amount;
                    if (nowAmount <= beforeAmount) {
                        price = Math.max(nowData.low, money * flag);
                    }
                    double sell = money / 0.998001;
                    System.out.println(name + "智能卖出 成本价=" + sell + "   收盘价=" + price + "  净赚=" + df.format(price - sell));
                    payback += Double.valueOf(df.format(price - sell));
                    if (dealHandle != null) {
                        dealHandle.onDealSell(coinInfo, id, "0", df.format(price),String.valueOf(kLine.datas.get(kLine.datas.size() - 1).close));
                        sellFlag = true;
                    }
                }
            }

        }
        if (!buy) {
            System.out.println(name + "收入=" + df.format(payback));
        }
        return false;
    }

    @Override
    public boolean onTestHandle(CoinInfo coinInfo, int type,KLine kLine, TradesInfo tradesInfo, DepthInfo depthInfo) {
        DecimalFormat df = new DecimalFormat(".000000");
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
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            if (e12before < e26before && e12 > e26) {
                System.out.println("金叉=" + sdf.format(kLineData.get(i).date * 1000) + "  收盘价=" + kLineData.get(i).low);
                money = kLineData.get(i).low + (kLineData.get(i).high - kLineData.get(i).low) * 0.1;
//                System.out.println("购买价格=" + money);
                buy = true;
                sellFlag = false;
            } else if (e12before > e26before && e12 < e26 && buy) {
                System.out.println("死叉=" + sdf.format(kLineData.get(i).date * 1000) + "  收盘价=" + kLineData.get(i).close);
                KLine.Data data = kLineData.get(i);
                double price = data.low + (data.high - data.low) * 0.8;
                double sell = money / 0.998001;
                payback += Double.valueOf(df.format(price - sell));
//                    if (sell > kLineData.get(i).close) {
//                System.out.println("成本价=" + sell + "   收盘价=" + price + "  净赚=" + df.format(price - sell));
                buy = false;
            } else if (buy && !sellFlag) {
                KLine.Data nowData = kLineData.get(i);
                KLine.Data beforeData = kLineData.get(i - 1);
                System.out.println("当前价格=" + nowData.close + "   前一个价格=" + beforeData.close);
                if (nowData.close < beforeData.close) {
//                    System.out.println("阈值=" + money * flag + " money=" + money + "   flag=" + flag);
                    if (nowData.close >= money * flag) {

                        double temp = nowData.low + (nowData.high - nowData.low) * 0.8;
                        double price = temp >= money * flag ? temp : nowData.close;
                        double nowAmount = nowData.amount;
                        double beforeAmount = beforeData.amount;
                        if (nowAmount <= beforeAmount) {
                            price = Math.max(nowData.low, money * flag);
                        }
                        double sell = money / 0.998001;
                        System.out.println("智能卖出 " + sdf.format(nowData.date * 1000) + "成本价=" + sell + "   收盘价=" + price + "  净赚=" + df.format(price - sell));
                        payback += Double.valueOf(df.format(price - sell));
                        buy = false;
                    }
                }

            }
//            System.out.println("暂时的收入=" + df.format(payback));
        }
        System.out.println(name + "收入=" + df.format(payback));
        payback = 0;
        return false;
    }
}
