package strategy;

import soso.CoinInfo;
import soso.Dimension;
import soso.StrategyUtils;
import soso.Utils;
import soso.candlestickCharts.CandleType;
import soso.model.DepthInfo;
import soso.model.KLine;
import soso.model.TradesInfo;
import soso.strategydeal.DealHandle;
import soso.strategydeal.Strategy;
import wechat.MessageSend;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by zhoujia on 2017/6/24.
 */
public class TradeInfoStrategy implements Strategy {
    private static final double PRIME = 0.998001;
    private DecimalFormat df = new DecimalFormat(".00000");
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private int mBuyAmount = 500000;
    private int mSellAmount = 500000;
    /**
     * 买单价
     */
    private double bidPrice;
    private double mSellPrice = 0;
    private boolean isBuy;
    private int id = 0;
    /**
     * 每笔卖单最低盈利的比例
     */
    private double rate = 0.996001;
    /**
     * 总收益
     */
    private double income;
    /**
     * 价格在一定的时间内超过该阈值后不再自动交易
     */
    private double shakeThreshold = 0.01;

    /**
     * 暴涨暴跌定义的时间,以分钟为单位
     */
    private int shakeMin = 5;
    /**
     * 为了方便买卖单，设置的差价
     */
    private double diffPrice = 0.00001;
    /**
     * 亏损超过一定比例，就挂单
     */
    private double losePercent = 0.01;
    private int maxMin;
    private int maxInMin;
    private boolean isCandle = false;

    private boolean mRun;
    private int level;

    /**
     * @param buyAmount      在该数量的买单上设置买单
     * @param sellAmount     在该数量的卖单上设置卖单
     * @param shakeThreshold 价格在一定的时间内超过该阈值后不再自动交易
     * @param shakeMin       暴涨暴跌定义的时间,以分钟为单位
     * @param diffPrice      为了更顺利的买卖单，买单时加上diffprice,卖单时减去diffprice
     * @param losePercent    允许的亏损比例，超过该比例后，就自动挂单，开始下一次自动交易
     * @param rate           每笔卖单最低盈利的比例
     * @param priceCount     该币价格的小数点个数
     * @param maxMin         寻找当前时间点往前maxMin分钟之内的最高价格
     * @param maxInMin       在maxMin分钟内找到的最高价格是否在maxInMin分钟之内
     */
    public TradeInfoStrategy(int buyAmount, int sellAmount, double shakeThreshold, int shakeMin, double diffPrice,
                             double losePercent, double rate, int priceCount,
                             int maxMin, int maxInMin) {

        this.mBuyAmount = buyAmount;
        this.mSellAmount = sellAmount;
        this.shakeThreshold = shakeThreshold;
        this.shakeMin = shakeMin;
        this.diffPrice = diffPrice;
        this.losePercent = losePercent;
        this.rate = 1 - rate;
        this.maxMin = maxMin;
        this.maxInMin = maxInMin;
        StringBuilder sb = new StringBuilder();
        sb.append(".");
        for (int i = 0; i < priceCount; i++) {
            sb.append("0");

        }
        df = new DecimalFormat(sb.toString());
    }


    public TradeInfoStrategy() {
    }


    @Override
    public boolean onHandle(CoinInfo coinInfo, Dimension type, KLine kLine, TradesInfo tradesInfo, DepthInfo depthInfo, DealHandle dealHandle) {
        if (kLine == null || kLine.datas == null || depthInfo == null) {
            return false;
        }
        System.out.println("当前价格=" + String.valueOf(kLine.datas.get(kLine.datas.size() - 1).close));
        List<DepthInfo.Data> asks = depthInfo.ask;
        List<DepthInfo.Data> bids = depthInfo.bid;
        if (Utils.isEmpty(asks) || Utils.isEmpty(bids) || asks.size() < 9 || bids.size() < 9) {
            return false;
        }

        if (isBuy && type == Dimension.MIN_1) {
            if (!isCandle) {
                //先检查下当前的买单价格是否太低了
                List<DepthInfo.Data> bidsClone = new ArrayList<>(bids);
                Collections.sort(bidsClone, new SortComparator());

                List<DepthInfo.Data> asksClone = new ArrayList<>(asks);
                Collections.sort(asksClone, new SortComparator());

                if (!isStabilize(type, kLine)) {
                    if (dealHandle != null) {
                        int state = dealHandle.onDealBuyQuery(coinInfo, id);
                        if (state == DealHandle.STATE_WAIT) {
                            if (dealHandle.onDealBuyCancel(coinInfo, id, "", "", String.valueOf(kLine.datas.get(kLine.datas.size() - 1).close))) {
                                isBuy = false;
                                return false;
                            }
                            System.out.println("最新的买单价处于金叉向下，所以取消买单挂单");
                        }
                    }
                }

                //找到第一个过50w的买单
                boolean isFound = false;
                for (int i = 0; i < bidsClone.size(); i++) {
                    if (bidsClone.get(i).amount > getBuyAmount(kLine)) {
                        isFound = true;
                        double currentBidPrice = Double.parseDouble(df.format(bidsClone.get(i).price + diffPrice));
                        if (currentBidPrice != bidPrice) {
                            double totalAskAmount = 0;
                            for (int j = 0; j < asksClone.size(); j++) {
                                if (asksClone.get(j).price <= currentBidPrice / rate) {
                                    totalAskAmount += asksClone.get(j).amount;
                                }
                            }
                            double totalBidAmount = 0;
                            for (int j = 0; j < bidsClone.size(); j++) {
                                if (bidsClone.get(j).price >= currentBidPrice - diffPrice) {
                                    totalBidAmount += bidsClone.get(j).amount;
                                }
                            }
                            System.out.println("找到了最新的买单价" + currentBidPrice + " 当前力量值=" + totalBidAmount + " 前方阻力值=" + totalAskAmount);
                            if (totalBidAmount <= totalAskAmount) {
                                if (dealHandle != null) {
                                    int state = dealHandle.onDealBuyQuery(coinInfo, id);
                                    if (state == DealHandle.STATE_WAIT) {
                                        if (dealHandle.onDealBuyCancel(coinInfo, id, "", "", String.valueOf(kLine.datas.get(kLine.datas.size() - 1).close))) {
                                            isBuy = false;
                                        }
                                        System.out.println("前方阻力太大，取消设置最新的买单价");
                                    }
                                }
                                break;
                            }
                            if (dealHandle != null) {
                                int state = dealHandle.onDealBuyQuery(coinInfo, id);
                                if (state == DealHandle.STATE_WAIT) {
                                    //重新设置挂单价
                                    bidPrice = currentBidPrice;
                                    if (dealHandle.onDealBuyCancel(coinInfo, id, "", "", String.valueOf(kLine.datas.get(kLine.datas.size() - 1).close))) {
                                        if (dealHandle.onDealBuy(coinInfo, ++id, "", String.valueOf(bidPrice), String.valueOf(kLine.datas.get(kLine.datas.size() - 1).close))) {
                                            System.out.println("最新的买单价=" + bidPrice + "  数量=" + bidsClone.get(i).amount);
                                        } else {
                                            isBuy = false;
                                        }
                                    }
                                }
                            }
                        }
                        break;
                    }
                }
                //没有找到大单就取消买单
                if (!isFound) {
                    if (dealHandle != null) {
                        int state = dealHandle.onDealBuyQuery(coinInfo, id);
                        if (state == DealHandle.STATE_WAIT) {
                            if (dealHandle.onDealBuyCancel(coinInfo, id, "", "", String.valueOf(kLine.datas.get(kLine.datas.size() - 1).close))) {
                                isBuy = false;
                            }
                            System.out.println("没有找到大于" + mBuyAmount + "的单子，取消买单");
                        }
                    }
                }

            } else {
                System.out.println("锤子线不取消挂单");
            }
            //查找买单价格对应的现在的数量
            double firstPrice = bids.get(0).price;
            double lastPrice = bids.get(bids.size() - 1).price;

            if (bidPrice * (1 - losePercent) > kLine.datas.get(kLine.datas.size() - 1).close) {
                isBuy = false;
                System.out.println("已经损失" + (losePercent * 100) + "%, 挂单=" + (bidPrice / rate));
                if (dealHandle != null) {
                    int state = dealHandle.onDealSellQuery(coinInfo, id);
                    if (state == DealHandle.STATE_WAIT) {
                        dealHandle.onDealSell(coinInfo, id, "", String.valueOf(mSellPrice), String.valueOf(kLine.datas.get(kLine.datas.size() - 1).close));
//                        dealHandle.onDealSell(coinInfo, id, "", String.valueOf(asks.get(0).price), String.valueOf(kLine.datas.get(kLine.datas.size() - 1).close));
                        MessageSend.sendlossMessage(coinInfo.getCoinDepthTrades().coin, String.valueOf(losePercent * 100), String.valueOf(kLine.datas.get(kLine.datas.size() - 1).close), String.valueOf(bidPrice), String.valueOf((bidPrice / rate)));
                        dealHandle.onDealHang(id);
                    }

                }
                return false;
            }
            //找到买挂单中，买单的位置
            int index = 0;
            if (firstPrice < bidPrice) {
                System.out.println("买单价已经高于买1价了");
                index = 0;
            } else if (lastPrice > bidPrice) {
                System.out.println("买单价已经低于买8价了,设定为卖1价");
                index = 6;
            } else {
                for (int i = 0; i < 7; i++) {
                    index = i;
                    if (bids.get(i).price == bidPrice) {
                        break;
                    } else if (bids.get(i + 1).price == bidPrice) {
                        break;
                    } else {
                        if (bids.get(i).price > bidPrice && bids.get(i + 1).price < bidPrice) {
                            break;
                        }
                    }
                }
            }
            //计算买单价之上的所有数量
            int bidTotalAmount = 0;
            if (index > 0) {
                for (int i = 0; i <= Math.min(7, index); i++) {
                    bidTotalAmount += bids.get(i).amount;
                }
            }
            double sellPrice = 0;
            if (index == 6) {
                System.out.println("设定卖价为卖一=" + asks.get(0).price);
                sellPrice = asks.get(0).price;
            } else {
                int askTotalAmount = 0;
                double max = 0.6;
                for (int i = 0; i < asks.size(); i++) {
                    max -= 0.02;
                    askTotalAmount += asks.get(i).amount;
                    if (asks.get(i).amount >= mSellAmount || askTotalAmount >= bidTotalAmount * max) {
                        sellPrice = asks.get(i).price;
                        //如果是卖一就不减去diffprice
                        if (i != 0) {
                            sellPrice -= diffPrice;
                        }
                        System.out.println("动态计算的交易卖价=" + sellPrice + "   最低卖价=" + (bidPrice / rate));
                        sellPrice = Math.max(sellPrice, bidPrice / rate);
                        System.out.println("卖单数量=" + asks.get(i).amount + "  卖单价=" + asks.get(i).price + "  买单价=" + bidPrice + "  rate=" + rate + "   成本价=" + (bidPrice / rate));
                        break;
                    }
                }
            }

            sellPrice = Double.parseDouble(df.format(Math.max(sellPrice, bidPrice / rate)));
            if (mSellPrice == 0) {
                mSellPrice = sellPrice;

                if (dealHandle != null) {
                    int state = dealHandle.onDealBuyQuery(coinInfo, id);
                    if (state == DealHandle.STATE_DONE) {
                        if (dealHandle.onDealSell(coinInfo, id, "", String.valueOf(mSellPrice), String.valueOf(kLine.datas.get(kLine.datas.size() - 1).close))) {
                            System.out.println("卖单价=" + mSellPrice);
                        } else {
                            mSellPrice = 0;
                            System.out.println("买单交易出错---未完成---->");
                        }

                    } else {
                        mSellPrice = 0;
                        System.out.println("买单交易未完成---->");
                    }
                }

            } else
                //两次卖单的价格不一致
                if (mSellPrice != sellPrice) {
                    double oldSellPrice = sellPrice;
                    mSellPrice = sellPrice;
                    //查询订单是否成交了,如果没成交，就应该取消之前的然后换成挂新卖单
                    System.out.println("新的卖单价=" + mSellPrice);
                    if (dealHandle != null) {
                        int state = dealHandle.onDealSellQuery(coinInfo, id);
                        if (state == DealHandle.STATE_WAIT) {
                            if (!dealHandle.onDealSell(coinInfo, id, "", String.valueOf(mSellPrice), String.valueOf(kLine.datas.get(kLine.datas.size() - 1).close))) {
                                mSellPrice = 0;
                            }
                        } else if (state == DealHandle.STATE_DONE) {
                            mSellPrice = oldSellPrice;
                        }
                    }
                }
            if (dealHandle != null) {
                int state = dealHandle.onDealSellQuery(coinInfo, id);
                if (state == DealHandle.STATE_DONE) {
                    isCandle = false;
                    isBuy = false;
                    double cost = bidPrice / PRIME;
                    income += (mSellPrice - cost);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String now = sdf.format(System.currentTimeMillis());
                    String report = now + "本次收益： 买单价=" + bidPrice + "  成本价=" + cost + " 卖单价=" + mSellPrice + "   收益=" + df.format((mSellPrice - cost)) +
                            "\n最新收益：" + df.format(income);
                    System.err.println(report);
                    MessageSend.sendNormalMessage(coinInfo.getCoinDepthTrades().coin, String.valueOf(bidPrice), String.valueOf(mSellPrice), df.format((mSellPrice - cost)), df.format(income));
                }
            }
        } else {
            double buyPrice = 0;
//            CandleType candleType = CandleFactory.create(kLine);
            CandleType candleType = CandleType.NONE;
            if (candleType == CandleType.HAMMER && type == Dimension.MIN_1) {
                buyPrice = kLine.datas.get(kLine.datas.size() - 2).close;
                System.out.println("锤子线,设定价格为=" + buyPrice);
                isCandle = true;
            } else {
                isCandle = false;
                if (!isStabilize(type, kLine)) {
                    return false;
                }
                buyPrice = findFirstBigDeal(kLine, bids, asks);
            }
            if (buyPrice != 0) {
                bidPrice = buyPrice;
                id++;
                mSellPrice = 0;
                if (dealHandle != null) {
                    if (dealHandle.onDealBuy(coinInfo, id, "", String.valueOf(bidPrice), String.valueOf(kLine.datas.get(kLine.datas.size() - 1).close))) {
                        isBuy = true;
                    }
                }
                System.out.println("买单价=" + bidPrice);
            }
        }
        return false;
    }


    private double findFirstBigDeal(KLine kLine, List<DepthInfo.Data> bids, List<DepthInfo.Data> asks) {

        bids = new ArrayList<>(bids);
        asks = new ArrayList<>(asks);
        asks.sort(new SortComparator());
        bids.sort(new SortComparator());
        for (int i = 0; i < bids.size(); i++) {
            System.out.println("买单价=" + bids.get(i).price + "    数量=" + bids.get(i).amount);
            if (bids.get(i).amount > getBuyAmount(kLine)) {
                double buyPrice = Double.parseDouble(df.format(bids.get(i).price + diffPrice));
                double totalAskAmount = 0;
                for (DepthInfo.Data ask : asks) {
                    if (ask.price <= buyPrice / rate) {
                        System.out.println("前方阻力=" + ask.price + "   数量=" + ask.amount);
                        totalAskAmount += ask.amount;
                    }
                }
                double totalBidAmount = 0;
                for (int j = 0; j < bids.size(); j++) {
                    if (bids.get(j).price >= buyPrice - diffPrice) {
                        totalBidAmount += bids.get(j).amount;
                    }
                }
                if (totalBidAmount <= totalAskAmount) {
                    return 0;
                }
                return buyPrice;
            }
        }
        return 0;
    }

    //根据当前的金叉死叉，对mBuyAmount进行调节
//    private int getBuyAmount(KLine kLine) {
//        if (kLine == null) {
//            return mBuyAmount;
//        }
//        double[] ema12 = StrategyUtils.getCloseEMA(kLine, 2, 7);
//        double[] ema26 = StrategyUtils.getCloseEMA(kLine, 2, 30);
//        if (ema12[ema12.length - 1] > ema26[ema26.length - 1]) {
//            return mBuyAmount;
//        }
//        int index = 0;
//        for (int i = ema12.length - 1; i >= 0; i--) {
//            if (ema12[i] > ema26[i]) {
//                index = i;
//                break;
//            }
//        }
//        int maxIndex = 0;
//        double minEma = Double.MAX_VALUE;
//        for (int i = index; i < ema12.length; i++) {
//            if (ema12[i] < minEma) {
//                minEma = ema12[i];
//                maxIndex = i;
//            }
//        }
//        if (maxIndex == ema12.length - 1) {
//            System.out.println("当前是死叉最低价格，设定的数量为=" + (int) (mBuyAmount * 1.5));
//            return (int) (mBuyAmount * 1.5);
//        }
//        return mBuyAmount;
//    }


    private int getBuyAmount(KLine kLine) {
//        if (kLine == null) {
//            return mBuyAmount;
//        }
//        double[] ema12 = StrategyUtils.getCloseEMA(kLine, 2, 7);
//        double[] ema26 = StrategyUtils.getCloseEMA(kLine, 2, 30);
//        if (ema12[ema12.length - 1] > ema26[ema26.length - 1]) {
//            return mBuyAmount;
//        }
//        int index = 0;
//        for (int i = ema12.length - 1; i >= 0; i--) {
//            if (ema12[i] > ema26[i]) {
//                index = i;
//                break;
//            }
//        }
//        int maxIndex = 0;
//        double minEma = Double.MAX_VALUE;
//        for (int i = index; i < ema12.length; i++) {
//            if (ema12[i] < minEma) {
//                minEma = ema12[i];
//                maxIndex = i;
//            }
//        }
//        if (maxIndex == ema12.length - 1) {
//            System.out.println("当前是死叉最低价格，设定的数量为=" + (int) (mBuyAmount * 1.5));
//            return (int) (mBuyAmount * 1.5);
//        }
        double  multiply = 1;
        if (level == 1) {
            multiply = 2;
        } else if (level == 2) {
            multiply = 0.5;
        }
        return (int) (mBuyAmount * multiply);
    }


    private boolean isStabilize(Dimension type, KLine kLine) {
        if (kLine == null) {
            return false;
        }
        List<KLine.Data> datas = kLine.getKLine().datas;
        if (datas == null) {
            return false;
        }
        double[] ema7 = StrategyUtils.getCloseEMA(kLine, 2, 7);
        double[] ema30 = StrategyUtils.getCloseEMA(kLine, 2, 30);
        switch (type) {
            case MIN_1:
                if (!mRun) {
                    return false;
                }
                //找到最近maxMin分钟之内的最高价格，是否在当前价格的多少分钟之内
                int index = 0;
                double maxPrice = Double.MIN_VALUE;
                for (int i = datas.size() - maxMin; i < datas.size(); i++) {
                    double price = datas.get(i).close;
                    if (maxPrice < price) {
                        maxPrice = price;
                        index = i;
                    }
                }

                //如果最高价格和当前价格在同一条金叉上，不买
                if (ema7.length > index && ema30.length > index && ema7[index] > ema30[index]) {
                    boolean isSame = true;
                    for (int i = index; i < ema7.length - 1; i++) {
                        if (ema7[i] < ema30[i]) {
                            isSame = false;
                            break;
                        }
                    }
                    if (isSame) {
                        System.out.println("最高价格和当前价格在同一条金叉上");
                        return false;
                    }
                }
                //如果当前在金叉向下的话就不买了，风险太大了
                if (ema7.length > 2 && ema30.length > 2) {
                    if (ema7[ema7.length - 2] > ema30[ema30.length - 2] && ema7[ema7.length - 2] < ema7[ema7.length - 3]) {
                        System.out.println("当前处于金叉向下，不买" + sdf.format(datas.get(datas.size() - 1).date * 1000));
                        return false;
                    } else if (ema7[ema7.length - 2] < ema30[ema30.length - 2] &&
                            (ema7[ema7.length - 3] >= ema30[ema30.length - 3] ||
                                    ema7[ema7.length - 4] >= ema30[ema30.length - 4])) {
                        System.out.println("前两分钟还是金叉，不买");
                        return false;
                    }
                }


                //如果shake分钟之内波动太大，也不买
                if (datas.size() >= shakeMin) {
                    for (int j = datas.size() - shakeMin + 1; j < datas.size(); j++) {
                        for (int i = datas.size() - shakeMin; i < j; i++) {
//                        if (datas.get(i).high * (1 + shakeThreshold) <= datas.get(j).high) {
//                            System.out.println("最高值变化浮动已超过阈值，暂停买单");
//                            return false;
//                        } else if (datas.get(i).low >= datas.get(j).low * (1 + shakeThreshold)) {
//                            System.out.println("最低值变化浮动已超过阈值，暂停买单");
//                            return false;
//                        } else
                            if (datas.get(i).high >= datas.get(j).low * (1 + shakeThreshold)) {
                                System.out.println("最低值变化浮动已超过阈值，暂停买单");
                                return false;

                            } else if (datas.get(i).low * (1 + shakeThreshold) <= datas.get(j).high) {
                                System.out.println("最高值变化浮动已超过阈值，暂停买单");
                                return false;
                            }
                        }
                    }
                }
                return true;
            case MIN_15:
                boolean flag = true;
                for (int i = ema7.length - 2; i < ema7.length - 1; i++) {
                    if (ema7[i] >= ema7[i + 1]) {
                        System.out.println("当前处于15分钟线，并且处于向下");
                        flag = false;
                        break;
                    }
                }
                if (!flag) {
                    mRun = false;
                    return false;
                }
                mRun = true;
                if (ema7[ema7.length - 1] > ema30[ema30.length - 1]) {
                    level = 2;
                } else {
                    level = 1;
                }

                return false;
            default:
                return false;
        }


//        if (datas != null) {
//            if (datas.size() <= 60) {
//                return false;
//            }
//            double[] ema12 = StrategyUtils.getCloseEMA(kLine, 2, 7);
//            double[] ema26 = StrategyUtils.getCloseEMA(kLine, 2, 30);
//            double badAvg = 0;
//            double goodAvg = 0;
////            double avgAmount = 0;
//            int count = 0;
//            for (int i = Math.max(0, datas.size() - 360); i < datas.size(); i++) {
//                if (ema12[i] < ema26[i]) {
//                    count++;
//                    badAvg += datas.get(i).close;
//                }
//            }
//            int goodCount = 0;
//            for (int i = Math.max(0, datas.size() - 360); i < datas.size(); i++) {
//                goodAvg += datas.get(i).close;
//                goodCount++;
//            }
//            badAvg /= count;
//            goodAvg /= goodCount;
//            double current = datas.get(datas.size() - 1).close;
////            System.out.println("平均交易量=" + avgAmount);
//            //当价格超过平均价格阈值之上时，风险太大
//            double todayOpen = datas.get(0).close;
//            double todayClose = datas.get(datas.size() - 1).close;
//            if (badAvg * (1 + 0.02) <= goodAvg) {
//                System.out.println("市场抖动，风险极大 当日均价=" + goodAvg + " 当前价格=" + current + "   " + badAvg);
//                return false;
//            } else {
//                if (goodAvg * (1 + 0.05) <= current) {
//                    System.out.println("市场极强，风险极大 当日均价" + goodAvg + " 当前价格=" + current + "  阈值=" + (badAvg));
//                    return false;
//                }
//                System.out.println("市场稳定，当日均价=" + goodAvg + " 当前价格=" + current + "  阈值=" + (badAvg));
//            }
//            if (badAvg * (1 + 0.05) <= goodAvg) {
//                if (current >= badAvg) {
//                    System.out.println("市场趋弱，当日均价=" + badAvg + " 当前价格=" + current + "  阈值=" + (badAvg));
//                    return false;
//                }
//            } else if (badAvg * (1 + 0.05) < goodAvg || goodAvg * (1 + 0.05) <= badAvg) {
//                if (current >= goodAvg) {
//                    System.out.println("市场趋强，当日均价=" + goodAvg + " 当前价格=" + current + "  阈值=" + (goodAvg));
//                    return false;
//                }
//            } else {
//                System.out.println("市场极强，风险极大 当日均价=" + goodAvg + " 当前价格=" + current + "   " + badAvg);
//                return false;
//            }


//            System.out.println("在" + maxMin + "分钟内，找到的最高价格为=" + maxPrice + ",距离当前时间点的分钟间隔为=" + (datas.size() - index));
//            if (datas.size() - index <= maxInMin) {
//                return false;
//            }


        //找到最高值所在的金叉的持续时间
//            int golden = 0;
//            int death = 0;
//            for (int i = index; i >= 0; i--) {
//                if (ema12[i] > ema26[i] && ema12[i - 1] < ema26[i - 1]) {
//                    golden = i;
//                    break;
//                }
//            }
//            for (int i = index; i < ema12.length - 1; i++) {
//                if (ema12[i] > ema26[i] && ema12[i + 1] < ema26[i + 1]) {
//                    death = i;
//                    break;
//                }
//            }
//            if (golden != 0 && death != 0 && death - golden > 0) {
//                int diffTime = death - golden;
//                //最高价跌到金叉之下的一定时间内也不交易
//                if (ema12.length <= diffTime + death) {
//                    System.out.println("最高价所在金叉持续时间=" + diffTime + "分钟，暂不交易");
//                    return false;
//                }
//            }

    }

    @Override
    public boolean onTestHandle(CoinInfo coinInfo, Dimension type, KLine kLine, TradesInfo tradesInfo, DepthInfo depthInfo) {
        System.out.println(sdf.format(kLine.datas.get(kLine.datas.size() - 1).date * 1000) + type);

        return false;
    }

    private static class SortComparator implements Comparator<DepthInfo.Data> {

        @Override
        public int compare(DepthInfo.Data o1, DepthInfo.Data o2) {
            if (o1 == null || o2 == null) {
                return 0;
            }

            return o1.price > o2.price ? -1 : o1.price < o2.price ? 1 : 0;
        }
    }

}
