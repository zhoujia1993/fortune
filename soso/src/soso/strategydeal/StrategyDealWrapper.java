package soso.strategydeal;

/**
 * Created by zhoujia on 2017/6/17.
 */
public class StrategyDealWrapper {
    private Strategy strategy;
    private DealHandle dealHandle;

    public StrategyDealWrapper(Strategy strategy, DealHandle dealHandle) {
        this.strategy = strategy;
        this.dealHandle = dealHandle;
    }

    public Strategy getStrategy() {
        return strategy;
    }

    public DealHandle getDealHandle() {
        return dealHandle;
    }
}
