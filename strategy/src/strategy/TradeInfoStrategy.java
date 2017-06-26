package strategy;

import soso.CoinInfo;
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
    private int mBuyAmount = 500000;
    private int mSellAmount = 500000;
    /**
     * 买单价
     */
    private double bidPrice;
    private double mSellPrice = 0;
    private boolean isBuy;
    private int id = 0;
    private double RATE = 0.997001;
    //    private double RATE = 0.998001;
    private double income;

    public TradeInfoStrategy(int buyAmount, int sellAmount) {
        this.mBuyAmount = buyAmount;
        this.mSellAmount = sellAmount;
        df = new DecimalFormat(".00000");
    }

    @Override
    public boolean onHandle(CoinInfo coinInfo, KLine kLine, TradesInfo tradesInfo, DepthInfo depthInfo, DealHandle dealHandle) {
        if (kLine == null || depthInfo == null) {
            return false;
        }
        List<DepthInfo.Data> asks = depthInfo.ask;
        List<DepthInfo.Data> bids = depthInfo.bid;
        if (Utils.isEmpty(asks) || Utils.isEmpty(bids)) {
            return false;
        }

        if (isBuy) {
            //先检查下当前的买单价格是否太低了
            List<DepthInfo.Data> bidsClone = new ArrayList<>(bids);
            Collections.sort(bidsClone, new SortComparator());

            List<DepthInfo.Data> asksClone = new ArrayList<>(asks);
            Collections.sort(asksClone, new SortComparator());

            //找到第一个过50w的买单
            for (int i = 1; i < bidsClone.size(); i++) {
                if (bidsClone.get(i).amount > mBuyAmount) {
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
                            return false;
                        }

                        if (dealHandle != null) {
                            int state = dealHandle.onDealBuyQuery(coinInfo, id);
                            if (state == DealHandle.STATE_WAIT) {
                                //重新设置挂单价
                                bidPrice = currentBidPrice;
                                dealHandle.onDealBuyCancel(coinInfo, id, "", "");
                                dealHandle.onDealBuy(coinInfo, ++id, "", String.valueOf(bidPrice));
                                System.out.println("最新的买单价=" + bidPrice + "  数量=" + bidsClone.get(i).amount);
                            }
                        }
                    }
                    break;
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
                        dealHandle.onDealSell(coinInfo, id, "", String.valueOf(mSellPrice));
                    }

                }
                return false;
            }
            if (firstPrice < bidPrice) {
                System.out.println("买单价已经高于买1价了");

                amount = bids.get(0).amount;
                index = 0;
            } else if (lastPrice > bidPrice) {
                System.out.println("买单价已经低于买最低价了");
                amount = bids.get(bids.size() - 1).amount;
                index = bids.size() - 1;
            } else {
                for (int i = 0; i < bids.size() - 1; i++) {
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
            for (int i = 0; i <= Math.min(bids.size() - 1, index); i++) {
                bidTotalAmount += bids.get(i).amount;
            }
            int askTotalAmount = 0;
            double sellPrice = 0;
            for (int i = 0; i < asks.size(); i++) {
                askTotalAmount += asks.get(i).amount;
                if (asks.get(i).amount >= mSellAmount || askTotalAmount >= bidTotalAmount * 0.8) {
                    System.out.println("交易卖价=" + asks.get(i).price + "   最低卖价=" + (bidPrice / RATE));
                    sellPrice = Double.parseDouble(df.format(Math.max(asks.get(i).price - 0.00001, bidPrice / RATE)));
                    System.out.println("卖单数量=" + asks.get(i).amount + "  卖单价=" + asks.get(i).price + "  买单价=" + bidPrice + "  rate=" + RATE + "   成本价=" + (bidPrice / RATE));
                    break;
                }
            }
            if (mSellPrice == 0) {
                mSellPrice = sellPrice;
                for(int i=0;i<bids.size();i++) {
                    if (bids.get(i).price == bidPrice) {
                        System.out.println("买单价=" + bidPrice + "   数量=" + bids.get(i).amount);
                    }
                }
                if (dealHandle != null) {
                    int state = dealHandle.onDealBuyQuery(coinInfo, id);
                    if (state == DealHandle.STATE_DONE) {
                        dealHandle.onDealSell(coinInfo, id, "", String.valueOf(mSellPrice));
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
                            dealHandle.onDealSell(coinInfo, id, "", String.valueOf(mSellPrice));
                        } else if (state == DealHandle.STATE_DONE) {
                            isBuy = false;
                            double cost = bidPrice / 0.998001;
                            income += (oldSellPrice - cost);
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                            String now = sdf.format(System.currentTimeMillis());
                            System.err.println(now + "本次收益： 买单价=" + bidPrice + "  成本价=" + cost + " 卖单价=" + oldSellPrice + "   收益=" + df.format((oldSellPrice - cost)));
                            System.err.println(now + "最新收益：" + df.format(income));
                        }
                    }
                }
        } else {
            //如果当前在金叉台上面的话就不买了，风险太大了
//            List<KLine.Data> datas = kLine.getKLine().datas;
//            double[] ema12 = StrategyUtils.getCloseEMA(kLine, 2, 7);
//            double[] ema26 = StrategyUtils.getCloseEMA(kLine, 2, 30);
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
                    isBuy = true;
                    mSellPrice = 0;
                    if (dealHandle != null) {
                        dealHandle.onDealBuy(coinInfo, id, "", String.valueOf(bidPrice));
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
    public boolean onTestHandle(CoinInfo coinInfo, KLine kLine, TradesInfo tradesInfo, DepthInfo depthInfo) {
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
