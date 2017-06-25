package trade;

import soso.CoinInfo;
import soso.strategydeal.DealHandle;
import yunbi.YunBi;
import yunbi.model.Order;

import java.util.HashMap;
import java.util.Map;

public class SmartGDFTrade implements DealHandle {

    private String market;
    private Map<Integer, Order> buyOrders;
    private Map<Integer, Order> sellOrders;

    public SmartGDFTrade(String market) {
        this.market = market;
        buyOrders = new HashMap<>();
        sellOrders = new HashMap<>();
    }

    @Override
    public void onDealBuy(CoinInfo coinInfo, int id, String amount, String price) {
        if (!buyOrders.containsKey(id)) {
            amount = String.valueOf(10);
            buyOrders.put(id, YunBi.getInstance().createOrder(market, amount, price, true));
            System.out.println("购买了" + amount + "数量，价格=" + price);
        }
    }

    @Override
    public void onDealSell(CoinInfo coinInfo, int id, String amount, String price) {
        if (!buyOrders.containsKey(id) || buyOrders.get(id) == null) {
            return;
        }
        if (sellOrders.containsKey(id)) {
            Order sellOrder = sellOrders.get(id);
            if (sellOrder != null) {
                Order queryOrder = YunBi.getInstance().queryOrder(String.valueOf(sellOrder.id));
                if (queryOrder != null) {
                    if (queryOrder.isWait()) {
                        queryOrder = YunBi.getInstance().cancelOrder(String.valueOf(queryOrder.id));
                        if(queryOrder!=null){
                        sellOrder = YunBi.getInstance().createOrder(market, String.valueOf(Double.valueOf(queryOrder.volume)), price, false);
                        sellOrders.put(id, sellOrder);}
                    }
                }

            }
        } else {
            Order queryOeder = YunBi.getInstance().queryOrder(String.valueOf(buyOrders.get(id).id));
            if (queryOeder != null) {
                Order sellOrder = null;
                if (queryOeder.isDone()) {
                    sellOrder = YunBi.getInstance().createOrder(market, String.valueOf(Double.valueOf(queryOeder.volume) - Double.valueOf(queryOeder.remainVolume)), price, false);
                    System.out.println("卖出了" + amount + "数量，价格=" + price);
                    if (sellOrder != null) {
                        sellOrders.put(id, sellOrder);
                    }
                } else if (queryOeder.isWait()) {
                    sellOrder = YunBi.getInstance().cancelOrder(String.valueOf(queryOeder.id));
                    System.out.println("取消了" + amount + "数量，价格=" + price);
                }

            }
        }
    }

    @Override
    public void onDealBuyCancel(CoinInfo coinInfo, int id, String amount, String price) {
        if (!buyOrders.containsKey(id) || buyOrders.get(id) == null) {
            return;
        }
        YunBi.getInstance().cancelOrder(String.valueOf(buyOrders.get(id).id));
    }

    @Override
    public void onDealSellCancel(CoinInfo coinInfo, int id, String amount, String price) {
        if (!sellOrders.containsKey(id)) {
            return;
        }
        YunBi.getInstance().cancelOrder(String.valueOf(sellOrders.get(id).id));
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


