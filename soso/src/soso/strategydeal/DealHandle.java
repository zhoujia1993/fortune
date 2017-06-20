package soso.strategydeal;

import soso.CoinInfo;

/**
 * 回调方法有点多，看实际情况调用
 * Created by zhoujia on 2017/6/17.
 */
public interface DealHandle {
    /**
     * 挂单状态
     */
    public static final int STATE_WAIT = 0;
    /**
     * 单子完成交易
     */
    public static final int STATE_DONE = 1;
    /**
     * 交易被取消
     */
    public static final int STATE_CANCEL = 2;
    /**
     * 状态未知
     */
    public static final int STATE_UNKNOWN = 3;

    /**
     * 买单回调
     * @param coinInfo
     * @param id
     * @param amount
     * @param price
     */
    void  onDealBuy(CoinInfo coinInfo, int id, String amount, String price);

    /**
     * 卖单回调
     * @param coinInfo
     * @param id
     * @param amount
     * @param price
     */
    void onDealSell(CoinInfo coinInfo, int id, String amount, String price);

    /**
     * 取消买单
     * @param coinInfo
     * @param id
     * @param amount
     * @param price
     */
    void onDealBuyCancel(CoinInfo coinInfo, int id, String amount, String price);
    void onDealSellCancel(CoinInfo coinInfo, int id, String amount, String price);

    /**
     * 查询买单，并返回订单状态
     *
     * @param coinInfo
     * @param id
     * @return 0=wait,1=done,2=cancel,3=unknown
     */
    int onDealBuyQuery(CoinInfo coinInfo, int id);

    /**
     * 查询卖单，并返回订单状态
     * @param coinInfo
     * @param id
     * @return
     */
    int onDealSellQuery(CoinInfo coinInfo, int id);

}
