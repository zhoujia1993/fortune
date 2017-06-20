package soso;

/**
 * Created by zhoujia on 2017/6/17.
 */
public class CoinInfo {


    private CoinKLine coinKLine;
    private CoinDepthTrades coinDepthTrades;

    public CoinInfo(String lan, String market, String coin, String symbol, int size, int type) {
        this.coinDepthTrades = new CoinDepthTrades(lan, market, coin, size);
        this.coinKLine = new CoinKLine(lan, symbol, type, 0);
    }

    /**
     * 这几个值需要抓sosobtc的接口，在不同的币的详情页的两个接口理由，很好抓
     * @param lan
     * @param market
     * @param coin
     * @param symbol
     * @param type
     */
    public CoinInfo(String lan, String market, String coin, String symbol, int type) {
        this(lan, market, coin, symbol, 50, type);
    }

    public CoinKLine getCoinKLine() {
        return coinKLine;
    }

    public CoinDepthTrades getCoinDepthTrades() {
        return coinDepthTrades;
    }

    public static class CoinDepthTrades {

        public String lan;
        public String market;
        public String coin;
        public int size;


        public CoinDepthTrades(String lan, String market, String coin, int size) {
            this.lan = lan;
            this.market = market;
            this.coin = coin;
            this.size = size;
        }

        public CoinDepthTrades(String lan, String market, String coin, String symbol) {
            this(lan, market, coin, 50);
        }


    }

    public static class CoinKLine {
        public String lan;
        public String symbol;
        public int type;
        public long since;

        public CoinKLine(String lan, String symbol, int type, long since) {
            this.lan = lan;
            this.symbol = symbol;
            this.type = type;
            this.since = since;
        }

    }
}
