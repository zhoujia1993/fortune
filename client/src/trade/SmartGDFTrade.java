package trade;

import soso.CoinInfo;
import soso.Utils;
import soso.strategydeal.DealHandle;
import yunbi.YunBi;
import yunbi.model.AccountInfo;
import yunbi.model.Order;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class SmartGDFTrade implements DealHandle {

    private String market;
    private Map<Integer, Order> buyOrders;
    private Map<Integer, Order> sellOrders;
    private Order emergencyOrder;
    /**
     * 一开始的初始钱数
     */
    private double balance;
    /**
     * 每次买单之后的钱数
     */
    private double nowBalance;

    private static final int DIVIDER = 3;

    public SmartGDFTrade(String market) {
        this.market = market;
        buyOrders = new HashMap<>();
        sellOrders = new HashMap<>();
        balance = syncAccount();

    }

    private double syncAccount() {
        AccountInfo info = YunBi.getInstance().syncAccountInfo();
        if (info != null && !Utils.isEmpty(info.accounts)) {
            for (AccountInfo.Account account : info.accounts) {
                if (account == null) {
                    continue;
                }
                if (account.currency.equals("cny")) {
                    double balance = Double.parseDouble(account.balance);
                    return balance;
                }
            }
        }
        return 0;
    }

    @Override
    public boolean onDealBuy(CoinInfo coinInfo, int id, String amount, String price, String currentPrice) {
        if (!buyOrders.containsKey(id)) {
            double money = syncAccount();
            System.out.println("账户当前余额=" + money + "   程序初始化账户余额=" + balance);
            if (money != 0 && money <= 10) {
                return false;
            }
            if (money > balance * (100 + DIVIDER) / 100) {
                System.out.println("账户当前余额>程序初始化账户余额10%,更新程序初始账户余额为当前余额");
                balance = money;
            }
            if (money != 0 && balance != 0 && money <= balance / DIVIDER) {
                if (emergencyOrder != null) {
                    Order queryOrder = YunBi.getInstance().queryOrder(String.valueOf(emergencyOrder.id));
                    if (queryOrder != null && queryOrder.isDone()) {
                        emergencyOrder = null;
                    }
                } else {
                    double maxPrice = Double.MIN_VALUE;
                    long maxId = -1;
                    Set<Map.Entry<Integer, Order>> sets = sellOrders.entrySet();
                    Iterator<Map.Entry<Integer, Order>> it = sets.iterator();
                    while (it.hasNext()) {
                        Map.Entry<Integer, Order> sellMap = it.next();
                        Order sellOrder = sellMap.getValue();
                        if (sellOrder == null) {
                            continue;
                        }
                        Order queryOrder = YunBi.getInstance().queryOrder(String.valueOf(sellOrder.id));
                        if (queryOrder == null) {

                            continue;
                        }
                        if (queryOrder.isCancelled() || queryOrder.isDone()) {
                            it.remove();
                        }

                        if (Double.parseDouble(queryOrder.price) > maxPrice) {
                            maxPrice = Double.parseDouble(queryOrder.price);
                            maxId = queryOrder.id;
                        }
                    }

                    if (maxId != -1) {
                        System.out.println("找到的价格最高的卖单=" + maxId + " 价格为=" + maxPrice);
                        Order queryOrder = YunBi.getInstance().queryOrder(String.valueOf(maxId));
                        if (queryOrder != null) {
                            if (queryOrder.isWait()) {
                                queryOrder = YunBi.getInstance().cancelOrder(String.valueOf(queryOrder.id));
                                if (queryOrder != null && queryOrder.isWait()) {
                                    Order sellOrder = YunBi.getInstance().createOrder(market, String.valueOf(Double.valueOf(queryOrder.volume)), price, false);
                                    if (sellOrder != null) {
                                        emergencyOrder = sellOrder;
                                        return false;
                                    }
                                }
                            }
                        }
                    }
                }
                return false;
            }

            amount = String.valueOf(balance / Double.parseDouble(price) / DIVIDER);
            Order order = YunBi.getInstance().createOrder(market, amount, price, true);
            if (order == null) {
                return false;
            }
            buyOrders.put(id, order);
            System.out.println("购买了" + amount + "数量，价格=" + price);
//            double money = syncAccount();
//            if (money <= balance / 10) {
//                double maxPrice = Double.MIN_VALUE;
//                int maxId = -1;
//                for (Map.Entry<Integer, Order> sellMap : sellOrders.entrySet()) {
//                    int sellId = sellMap.getKey();
//                    Order sellOrder = sellMap.getValue();
//                    if (sellOrder == null) {
//                        continue;
//                    }
//                    Order queryOrder = YunBi.getInstance().queryOrder(String.valueOf(sellOrder.id));
//                    if (queryOrder == null || queryOrder.isCancelled() || queryOrder.isDone()) {
//                        continue;
//                    }
//
//                    if (Double.parseDouble(queryOrder.price) > maxPrice) {
//                        maxPrice = Double.parseDouble(queryOrder.price);
//                        maxId = sellId;
//                    }
//                }
//                if (maxId != -1) {
//                    Order queryOrder = YunBi.getInstance().queryOrder(String.valueOf(maxId));
//                    if (queryOrder != null) {
//                        if (queryOrder.isWait()) {
//                            queryOrder = YunBi.getInstance().cancelOrder(String.valueOf(queryOrder.id));
//                            if (queryOrder != null && queryOrder.isWait()) {
//                                Order sellOrder = YunBi.getInstance().createOrder(market, String.valueOf(Double.valueOf(queryOrder.volume)), price, false);
//                                if (sellOrder != null) {
//                                    emergencyOrder = sellOrder;
//                                    return true;
//                                }
//                            }
//                        }
//                    }
//                }
//
//            }
            return true;
        }
        return false;
    }

    @Override
    public boolean onDealSell(CoinInfo coinInfo, int id, String amount, String price, String currentPrice) {
        if (!buyOrders.containsKey(id) || buyOrders.get(id) == null) {
            return false;
        }
        if (sellOrders.containsKey(id)) {
            Order sellOrder = sellOrders.get(id);
            if (sellOrder != null) {
                Order queryOrder = YunBi.getInstance().queryOrder(String.valueOf(sellOrder.id));
                if (queryOrder != null) {
                    if (queryOrder.isWait()) {
                        queryOrder = YunBi.getInstance().cancelOrder(String.valueOf(queryOrder.id));
                        if (queryOrder != null && queryOrder.isWait()) {
                            sellOrder = YunBi.getInstance().createOrder(market, String.valueOf(Double.valueOf(queryOrder.volume)), price, false);
                            if (sellOrder != null) {
                                sellOrders.put(id, sellOrder);
                                return true;
                            }
                        }
                    }
                }

            }
        } else {
            Order queryOeder = YunBi.getInstance().queryOrder(String.valueOf(buyOrders.get(id).id));
            if (queryOeder != null) {
                Order sellOrder = null;
                if (queryOeder.isDone()) {
                    //实际卖出的数量应该扣掉千一的手续费
                    sellOrder = YunBi.getInstance().createOrder(market, String.valueOf(Double.valueOf(queryOeder.volume) * 0.999), price, false);
                    System.out.println("卖出了" + String.valueOf(Double.valueOf(queryOeder.volume) * 0.999) + "数量，价格=" + price);
                    if (sellOrder != null) {
                        sellOrders.put(id, sellOrder);
                        return true;
                    }
                } else if (queryOeder.isWait()) {
                    sellOrder = YunBi.getInstance().cancelOrder(String.valueOf(queryOeder.id));
                    if (sellOrder != null) {
                        System.out.println("取消了" + String.valueOf(Double.valueOf(queryOeder.volume) * 0.999) + "数量，价格=" + price);
                        return true;
                    }

                }

            }
        }
        return false;
    }

    @Override
    public boolean onDealBuyCancel(CoinInfo coinInfo, int id, String amount, String price, String currentPrice) {
        if (!buyOrders.containsKey(id) || buyOrders.get(id) == null) {
            return false;
        }
        Order order = YunBi.getInstance().cancelOrder(String.valueOf(buyOrders.get(id).id));
        if (order != null) {
            return true;
        }
        return false;
    }

    @Override
    public boolean onDealSellCancel(CoinInfo coinInfo, int id, String amount, String price, String currentPrice) {
        if (!sellOrders.containsKey(id)) {
            return false;
        }

        Order order = YunBi.getInstance().cancelOrder(String.valueOf(sellOrders.get(id).id));
        if (order != null) {
            return true;
        }
        return false;
    }

    @Override
    public int onDealBuyQuery(CoinInfo coinInfo, int id) {
        if (buyOrders.containsKey(id) && buyOrders.get(id) != null) {
            Order order = YunBi.getInstance().queryOrder(String.valueOf(buyOrders.get(id).id));
            if (order != null) {
                return order.getState();
            } else {
                return DealHandle.STATE_UNKNOWN;
            }

        } else {
            return DealHandle.STATE_UNKNOWN;
        }
    }

    @Override
    public int onDealSellQuery(CoinInfo coinInfo, int id) {
        if (sellOrders.containsKey(id) && sellOrders.get(id) != null) {
            Order order = YunBi.getInstance().queryOrder(String.valueOf(sellOrders.get(id).id));
            if (order != null) {
                return order.getState();
            } else {
                return DealHandle.STATE_UNKNOWN;
            }

        } else {
            return DealHandle.STATE_UNKNOWN;
        }
    }

}


