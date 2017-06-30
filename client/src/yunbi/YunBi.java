package yunbi;

import soso.net.Method;
import soso.net.NameValuePair;
import yunbi.model.AccountInfo;
import yunbi.model.Markets;
import yunbi.model.Order;
import yunbi.net.YunBiNetService;

import java.util.List;

/**
 * Created by zhoujia on 2017/6/16.
 */
public class YunBi {

    private List<Markets> markets;
    private static final int MAX = 10;

    private YunBi() {

    }

    public static YunBi getInstance() {
        return YunBiInner.instance;
    }

    private static class YunBiInner {
        public static final YunBi instance = new YunBi();
    }


    public void init() {
        syncAccountInfo();
    }


    private void getAllMarkets() {
        markets = new YunBiNetService<List<Markets>>().request(Method.GET, "/api/v2/markets.json", null, false, Markets.class);
    }

    public  AccountInfo syncAccountInfo() {
        NameValuePair params = new NameValuePair();
        params.put("access_key", Config.ACCESS_KEY);
        params.put("tonce", String.valueOf(System.currentTimeMillis()));
        AccountInfo accountInfo = new YunBiNetService<AccountInfo>().request(Method.GET, "/api/v2/members/me.json", params, true, AccountInfo.class);
//        AccountCenter.getInstance().setAccount(accountInfo);
        return accountInfo;
    }

    public void getSpecificDeposit() {
        NameValuePair params = new NameValuePair();
        params.put("access_key", Config.ACCESS_KEY);
        params.put("tonce", String.valueOf(System.currentTimeMillis()));
        AccountInfo accountInfo = new YunBiNetService<AccountInfo>().request(Method.GET, "/api/v2/members/me.json", params, true, AccountInfo.class);
        AccountCenter.getInstance().setAccount(accountInfo);
    }

    public Order createOrder(String market, String amount, String price, boolean buy) {
        int count = 0;
        Order order = null;
        do {
            NameValuePair params = new NameValuePair();
            params.put("access_key", Config.ACCESS_KEY).put("market", market)
                    .put("price", price)
                    .put("side", buy ? "buy" : "sell")
                    .put("tonce", String.valueOf(System.currentTimeMillis()))
                    .put("volume", amount);
            order = new YunBiNetService<Order>().request(Method.POST, "/api/v2/orders.json", params, true, Order.class);
            System.out.println("创建单子:" + (order != null ? order.toString() : ""));
            count++;
            if (order == null) {
                try {
                    Thread.sleep(400);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        } while (order == null && count <= MAX);
        return order;
//        return null;
    }

    public Order queryOrder(String id) {
        int count = 0;
        Order order = null;
        do {
            NameValuePair params = new NameValuePair();
            params.put("access_key", Config.ACCESS_KEY).put("id", id)
                    .put("tonce", String.valueOf(System.currentTimeMillis()));
            order = new YunBiNetService<Order>().request(Method.GET, "/api/v2/order.json", params, true, Order.class);
            System.out.println("查询单子:" + (order != null ? order.toString() : ""));
            count++;
            if (order == null) {
                try {
                    Thread.sleep(400);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        } while (order == null && count <= MAX);
        return order;
    }

    public Order cancelOrder(String id) {
        int count = 0;
        Order order = null;
        do {
            NameValuePair params = new NameValuePair();
            params.put("access_key", Config.ACCESS_KEY).put("id", id)
                    .put("tonce", String.valueOf(System.currentTimeMillis()));
            order = new YunBiNetService<Order>().request(Method.POST, "/api/v2/order/delete.json", params, true, Order.class);
            System.out.println("取消单子:" + (order != null ? order.toString() : ""));
            count++;
            if (order == null) {
                try {
                    Thread.sleep(400);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        } while (order == null && count <= MAX);
        return order;
    }

}
