package soso;

import soso.strategydeal.StrategyDealWrapper;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by zhoujia on 2017/6/16.
 */
public class DataCenter {
    private static final int COOL_POOL_SIZE = 10;
    private static final int MAXIUM_POOL_SIZE = 128;
    private static final int KEEP_ALIVE = 1;
    private static final BlockingQueue QUEUE = new LinkedBlockingDeque(100000);
    private static final ThreadFactory FACTORY = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "DATACENTER #" + mCount.getAndIncrement());
        }
    };
    public ThreadPoolExecutor executor = new ThreadPoolExecutor(COOL_POOL_SIZE, MAXIUM_POOL_SIZE, KEEP_ALIVE, TimeUnit.SECONDS, QUEUE, FACTORY);
    private static CountDownLatch countDownLatch;


    public void start() {
        while (true) {
            Map<CoinInfo, List<StrategyDealWrapper>> strategies = Coin.getInstance().getAllStrategy();
            countDownLatch = new CountDownLatch(strategies.size());
            for (Map.Entry<CoinInfo, List<StrategyDealWrapper>> entry : strategies.entrySet()) {
                executor.execute(new DataFetchWorker(entry.getKey(), entry.getValue(), countDownLatch));
            }
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (Config.DEBUG) {
                break;
            }
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
