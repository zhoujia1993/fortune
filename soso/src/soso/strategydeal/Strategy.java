package soso.strategydeal;

import soso.CoinInfo;
import soso.model.DepthInfo;
import soso.model.KLine;
import soso.model.TradesInfo;

/**
 * Created by zhoujia on 2017/6/17.
 */
public interface Strategy {
    /**
     * 生产环境策略处理
     *@param type       策略对应的时间维度{@link soso.Dimension}
     * @param kLine      走势信息
     *
     *
     * @param tradesInfo 成交量信息
     * @param depthInfo  买盘/卖盘信息
     * @param dealHandle 买卖方法回调
     * @return true表示，不再处理后面的策略
     */
    boolean  onHandle(CoinInfo coinInfo,int type, KLine kLine, TradesInfo tradesInfo, DepthInfo depthInfo, DealHandle dealHandle);

    /**
     * 测试环境，可以基于历史数据模拟
     *
     * @param coinInfo
     * @param type       策略对应的时间维度{@link soso.Dimension}
     * @param kLine
     * @param tradesInfo
     * @param depthInfo
     * @return
     */
    boolean onTestHandle(CoinInfo coinInfo,int type, KLine kLine,TradesInfo tradesInfo, DepthInfo depthInfo);

}
