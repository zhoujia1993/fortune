package soso;

import soso.strategydeal.StrategyDealWrapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhoujia on 2017/6/16.
 */
public class Coin {

    private Map<CoinInfo, List<StrategyDealWrapper>> coinStrategy;

    private Coin() {
        coinStrategy = new HashMap<>();
    }

    public static Coin getInstance() {
        return CoinInner.instance;
    }

    public void registerStrategy(CoinInfo coinInfo, StrategyDealWrapper strategy) {
        List<StrategyDealWrapper> strategies;
        if (coinStrategy.containsKey(coinInfo)) {
            strategies = coinStrategy.get(coinInfo);
            if (strategies == null || strategies.size() == 0) {
                strategies = new ArrayList<>();
            }
            if (!strategies.contains(strategy)) {
                strategies.add(strategy);
            }
        } else {
            strategies = new ArrayList<>();
            strategies.add(strategy);
            coinStrategy.put(coinInfo, strategies);
        }
    }

    private static class CoinInner {
        public static final Coin instance = new Coin();
    }

    public List<StrategyDealWrapper> getSpecifyStrategy(CoinInfo coinInfo) {
        if (!coinStrategy.containsKey(coinInfo)) {
            return null;
        }
        return coinStrategy.get(coinInfo);
    }

    public Map<CoinInfo, List<StrategyDealWrapper>> getAllStrategy() {
        return coinStrategy;
    }
}
