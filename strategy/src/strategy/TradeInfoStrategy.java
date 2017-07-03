package strategy;

import soso.CoinInfo;
import soso.StrategyUtils;
import soso.Utils;
import soso.model.DepthInfo;
import soso.model.KLine;
import soso.model.TradesInfo;
import soso.strategydeal.DealHandle;
import soso.strategydeal.Strategy;

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
    private final DecimalFormat df;
    private final SimpleDateFormat sdf;
    private int mBuyAmount = 500000;
    private int mSellAmount = 500000;
    /**
     * 买单价
     */
    private double bidPrice;
    private double mSellPrice = 0;
    private boolean isBuy;
    private int id = 0;
    private double RATE = 0.996001;
    //    private double RATE = 0.998001;
    private double income;

    public TradeInfoStrategy(int buyAmount, int sellAmount) {
        this.mBuyAmount = buyAmount;
        this.mSellAmount = sellAmount;
        df = new DecimalFormat(".00000");
        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }

    @Override
    public boolean onHandle(CoinInfo coinInfo, int type, KLine kLine, TradesInfo tradesInfo, DepthInfo depthInfo, DealHandle dealHandle) {
        if (kLine == null || kLine.datas == null || depthInfo == null) {
            return false;
        }
        List<DepthInfo.Data> asks = depthInfo.ask;
        List<DepthInfo.Data> bids = depthInfo.bid;
        if (Utils.isEmpty(asks) || Utils.isEmpty(bids) || asks.size() < 9 || bids.size() < 9) {
            return false;
        }

        if (isBuy) {
            //先检查下当前的买单价格是否太低了
            List<DepthInfo.Data> bidsClone = new ArrayList<>(bids);
            Collections.sort(bidsClone, new SortComparator());

            List<DepthInfo.Data> asksClone = new ArrayList<>(asks);
            Collections.sort(asksClone, new SortComparator());


            //找到第一个过50w的买单
            boolean isFound = false;
            for (int i = 0; i < bidsClone.size(); i++) {

                if (bidsClone.get(i).amount > mBuyAmount) {
                    isFound = true;
                    double currentBidPrice = Double.parseDouble(df.format(bidsClone.get(i).price + 0.00001));
                    if (currentBidPrice != bidPrice) {

                        double totalAskAmount = 0;
                        for (int j = 0; j < asksClone.size(); j++) {
                            if (asksClone.get(j).price <= currentBidPrice / RATE) {
                                System.out.println("前方阻力1=" + asksClone.get(j).price + "   数量=" + asksClone.get(j).amount);
                                totalAskAmount += asksClone.get(j).amount;
                            }
                        }
                        if (bidsClone.get(i).amount <= totalAskAmount) {
                            if (dealHandle != null) {
                                int state = dealHandle.onDealBuyQuery(coinInfo, id);
                                if (state == DealHandle.STATE_WAIT) {
                                    if (dealHandle.onDealBuyCancel(coinInfo, id, "", "", String.valueOf(kLine.datas.get(kLine.datas.size() - 1).close))) {
                                        isBuy = false;
                                    }
                                    System.out.println("前方阻力太大，取消买单");
                                }
                            }
                            break;
                        } else {
                            //如果当前在金叉向下的话就不买了，风险太大了
                            List<KLine.Data> datas = kLine.getKLine().datas;
                            if (datas != null) {
                                double[] ema12 = StrategyUtils.getCloseEMA(kLine, 2, 7);
                                double[] ema26 = StrategyUtils.getCloseEMA(kLine, 2, 30);
                                if (ema12.length > 2 && ema26.length > 2) {
                                    boolean cancel = false;
                                    if (ema12[ema12.length - 1] > ema26[ema26.length - 1] && ema12[ema12.length - 1] < ema12[ema12.length - 2]) {
                                        System.out.println("当前处于金叉向下1，取消买单" + datas.get(datas.size() - 1).date);
                                        cancel = true;
                                    } else if (ema12[ema12.length - 1] < ema26[ema26.length - 1] &&
                                            ema12[ema12.length - 3] >= ema26[ema26.length - 3]) {
                                        System.out.println("前两分钟还是金叉，取消买单");
                                        cancel = true;
                                    }
                                    if (cancel) {
                                        if (dealHandle != null) {
                                            int state = dealHandle.onDealBuyQuery(coinInfo, id);
                                            if (state == DealHandle.STATE_WAIT) {
                                                if (dealHandle.onDealBuyCancel(coinInfo, id, "", "", String.valueOf(kLine.datas.get(kLine.datas.size() - 1).close))) {
                                                    isBuy = false;
                                                }
                                                System.out.println("最新的买单价处于金叉向下，所以取消买单挂单");
                                            }
                                        }
                                        break;
                                    }
                                }
                            }
                        }

                        if (dealHandle != null) {
                            int state = dealHandle.onDealBuyQuery(coinInfo, id);
                            if (state == DealHandle.STATE_WAIT) {
                                //重新设置挂单价
                                bidPrice = currentBidPrice;
                                if (dealHandle.onDealBuyCancel(coinInfo, id, "", "", String.valueOf(kLine.datas.get(kLine.datas.size() - 1).close))) {
                                    dealHandle.onDealBuy(coinInfo, ++id, "", String.valueOf(bidPrice), String.valueOf(kLine.datas.get(kLine.datas.size() - 1).close));
                                    System.out.println("最新的买单价=" + bidPrice + "  数量=" + bidsClone.get(i).amount);
                                }

                            }
                        }
                    }
                    break;
                }
            }
            if (!isFound) {
                if (dealHandle != null) {
                    int state = dealHandle.onDealBuyQuery(coinInfo, id);
                    if (state == DealHandle.STATE_WAIT) {
                        if (dealHandle.onDealBuyCancel(coinInfo, id, "", "", String.valueOf(kLine.datas.get(kLine.datas.size() - 1).close))) {
                            isBuy = false;
                        }
                        System.out.println("没有找到大于50W的单子，取消买单");
                    }
                }
            }

            //查找买单价格现在的数量
            double firstPrice = bids.get(0).price;
            double lastPrice = bids.get(bids.size() - 1).price;
            int index = 0;
            double amount = 0;
            if (bidPrice * 0.99 > kLine.datas.get(kLine.datas.size() - 1).close) {
                isBuy = false;
                System.out.println("已经损失1%,挂单=" + (bidPrice / RATE));
                if (dealHandle != null) {
                    int state = dealHandle.onDealSellQuery(coinInfo, id);
                    if (state == DealHandle.STATE_WAIT) {
                        dealHandle.onDealSell(coinInfo, id, "", String.valueOf(mSellPrice), String.valueOf(kLine.datas.get(kLine.datas.size() - 1).close));
                    }

                }
                return false;
            }
            if (firstPrice < bidPrice) {
                System.out.println("买单价已经高于买1价了");

                amount = bids.get(0).amount;
                index = 0;
            } else if (lastPrice > bidPrice) {
                System.out.println("买单价已经低于买8价了");
                amount = bids.get(7).amount;
                index = 6;
            } else {
                for (int i = 0; i < 7; i++) {
                    index = i;
                    if (bids.get(i).price == bidPrice) {
                        amount = bids.get(i).amount;
                        break;
                    } else if (bids.get(i + 1).price == bidPrice) {
                        amount = bids.get(i + 1).amount;
                        break;
                    } else {
                        if (bids.get(i).price > bidPrice && bids.get(i + 1).price < bidPrice) {
                            amount = bids.get(i).amount;
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
                for (int i = 0; i < asks.size(); i++) {
                    askTotalAmount += asks.get(i).amount;
                    if (asks.get(i).amount >= mSellAmount || askTotalAmount >= bidTotalAmount * 0.6) {
                        System.out.println("交易卖价=" + asks.get(i).price + "   最低卖价=" + (bidPrice / RATE));
                        sellPrice = Math.max(asks.get(i).price - 0.00001, bidPrice / RATE);
                        System.out.println("卖单数量=" + asks.get(i).amount + "  卖单价=" + asks.get(i).price + "  买单价=" + bidPrice + "  rate=" + RATE + "   成本价=" + (bidPrice / RATE));
                        break;
                    }
                }
            }

            sellPrice = Double.parseDouble(df.format(Math.max(sellPrice, bidPrice / RATE)));
            if (mSellPrice == 0) {
                mSellPrice = sellPrice;
                for (int i = 0; i < bids.size(); i++) {
                    if (bids.get(i).price == bidPrice) {
                        System.out.println("买单价=" + bidPrice + "   数量=" + bids.get(i).amount);
                    }
                }
                if (dealHandle != null) {
                    int state = dealHandle.onDealBuyQuery(coinInfo, id);
                    if (state == DealHandle.STATE_DONE) {
                        dealHandle.onDealSell(coinInfo, id, "", String.valueOf(mSellPrice), String.valueOf(kLine.datas.get(kLine.datas.size() - 1).close));
                        System.out.println("卖单价=" + mSellPrice);
                    } else {
                        mSellPrice = 0;
                        System.out.println("买单交易未完成---->");
                    }
                }

            } else
                //两次卖单的价格不一致
                if (mSellPrice != sellPrice) {
                    double oldSellPrice = mSellPrice;
                    mSellPrice = sellPrice;
                    //查询订单是否成交了,如果没成交，就应该取消之前的然后换成挂新卖单
                    System.out.println("新的卖单价=" + mSellPrice);
                    if (dealHandle != null) {
                        int state = dealHandle.onDealSellQuery(coinInfo, id);
                        if (state == DealHandle.STATE_WAIT) {
                            dealHandle.onDealSell(coinInfo, id, "", String.valueOf(mSellPrice), String.valueOf(kLine.datas.get(kLine.datas.size() - 1).close));
                        } else if (state == DealHandle.STATE_DONE) {
                            isBuy = false;
                            double cost = bidPrice / 0.998001;
                            income += (oldSellPrice - cost);
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            String now = sdf.format(System.currentTimeMillis());
                            System.err.println(now + "本次收益： 买单价=" + bidPrice + "  成本价=" + cost + " 卖单价=" + oldSellPrice + "   收益=" + df.format((oldSellPrice - cost)));
                            System.err.println(now + "最新收益：" + df.format(income));
                        }
                    }
                }
        } else {
            //如果当前在金叉向下的话就不买了，风险太大了
            List<KLine.Data> datas = kLine.getKLine().datas;
            if (datas != null) {
                double[] ema12 = StrategyUtils.getCloseEMA(kLine, 2, 7);
                double[] ema26 = StrategyUtils.getCloseEMA(kLine, 2, 30);
                if (ema12.length > 2 && ema26.length > 2) {
                    if (ema12[ema12.length - 1] > ema26[ema26.length - 1] && ema12[ema12.length - 1] < ema12[ema12.length - 2]) {
                        System.out.println("当前处于金叉向下，不买" + sdf.format(datas.get(datas.size() - 1).date * 1000));
                        return false;
                    } else if (ema12[ema12.length - 1] < ema26[ema26.length - 1] &&
                            (ema12[ema12.length - 2] >= ema26[ema26.length - 2] ||
                                    ema12[ema12.length - 3] >= ema26[ema26.length - 3])) {
                        System.out.println("前两分钟还是金叉，不买");
                        return false;
                    }

                }
            }
            Collections.sort(asks, new SortComparator());
            Collections.sort(bids, new SortComparator());
            //找到第一个过50w的买单
            for (int i = 0; i < bids.size(); i++) {
                System.out.println("买单价=" + bids.get(i).price + "    数量=" + bids.get(i).amount);
                if (bids.get(i).amount > mBuyAmount) {
                    double buyPrice = Double.parseDouble(df.format(bids.get(i).price + 0.00001));
                    double totalAskAmount = 0;
                    for (int j = 0; j < asks.size(); j++) {
                        if (asks.get(j).price <= buyPrice / RATE) {
                            System.out.println("前方阻力=" + asks.get(j).price + "   数量=" + asks.get(j).amount);
                            totalAskAmount += asks.get(j).amount;
                        }
                    }
                    if (bids.get(i).amount <= totalAskAmount) {
                        return false;
                    }
                    bidPrice = buyPrice;
                    id++;
                    mSellPrice = 0;
                    if (dealHandle != null) {
                        if (dealHandle.onDealBuy(coinInfo, id, "", String.valueOf(bidPrice), String.valueOf(kLine.datas.get(kLine.datas.size() - 1).close))) {
                            isBuy = true;
                        }
//                        dealHandle.onDealBuy(coinInfo, id, "", String.valueOf(kLine.datas.get(kLine.datas.size() - 1).close));
                    }
                    System.out.println("买单价=" + bidPrice);
                    break;
                }
            }

        }
//        System.out.print(bids.get(bids.size() - 1));
//        System.out.println(depthInfo.toString());
        return false;
    }

    @Override
    public boolean onTestHandle(CoinInfo coinInfo, int type, KLine kLine, TradesInfo tradesInfo, DepthInfo depthInfo) {
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
