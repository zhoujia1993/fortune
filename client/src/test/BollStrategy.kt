package test

import soso.CoinInfo
import soso.Dimension
import soso.model.*
import soso.strategydeal.DealHandle
import soso.strategydeal.Strategy
import wechat.MessageSend
import yunbi.YunBi
import yunbi.model.Order
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*


/**
 * Created by zhoujia on 2017/7/22.
 */
class BollStrategy(private val market: String, val coinName: String, val diffPrice: Double, val losePercent: Double) : Strategy {
    data class Prime(val cost: Double, val amount: Double)
    enum class Strategy {
        MILD, Radical
    }

    companion object {
        @JvmStatic
        var totalIncome: Double = 0.0
    }

    private val bidOrders = arrayListOf<Order>()
    private var askOrders: Order? = null
    private var cost: Double = 0.0
    private var bidMoney: Double = 0.0
    private var bidAmount: Double = 0.0
    private var isBuy: Boolean = false

    private val df = DecimalFormat(".00000")
    private var readSellPrice: Double = 0.0
    private var sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    private var buyTime: Long = 0
    private var type: Strategy? = Strategy.MILD
    private var suspend = false
    private var tradePosition = 0.0

    enum class State {
        Bid, Ask, None, ASK_NOW, CANCEL, CANCELASK
    }

    private var balance: Double? = 150.0

    //    init {
//        val info = YunBi.getInstance().syncAccountInfo()
//        if (info != null && !Utils.isEmpty(info.accounts)) {
//            for (account in info.accounts) {
//                if (account == null) {
//                    continue
//                }
//                if (account.currency == "cny") {
//                    balance = java.lang.Double.parseDouble(account.balance)
//                }
//            }
//        } else {
//            exitProcess(1)
//        }
//    }
    fun getSellPrice(): Double = getCost().cost / 0.997


    /**
     * 持仓成本
     */
    fun getCost(): Prime {
        val it = bidOrders.iterator()
        var totalPrice: Double = 0.0
        var amount: Double = 0.0

        while (it.hasNext()) {
            var order = it.next()
            val queryOrder: Order? = YunBi.getInstance().queryOrder(order.id.toString())
            queryOrder?.let {
                if (it.isDone) {
                    isBuy = true
                    totalPrice += it.price.toDouble() * it.volume.toDouble()
                    amount += it.volume.toDouble()
                }
            }
        }
        cost = totalPrice / amount
        bidMoney = totalPrice / 0.999
        bidAmount = amount
        if (amount == 0.0) {
            return Prime(0.0, 0.0)
        }
        return Prime(totalPrice / (amount * 0.999), amount * 0.999)
    }

    fun getCostTimes(): Int = bidOrders.filter { YunBi.getInstance().queryOrder(it.id.toString())?.isDone ?: false }.size


    fun clearBidOrders() {
        val it = bidOrders.iterator()
        while (it.hasNext()) {
            var order = it.next()
            val queryOrder: Order? = YunBi.getInstance().queryOrder(order.id.toString())
            queryOrder?.let {
                if (it.isWait) {
                    YunBi.getInstance().cancelOrder(it.id.toString())
                }
            }
            it.remove()
        }

    }

    fun cancelBidOrders() {
        val it = bidOrders.iterator()
        while (it.hasNext()) {
            var order = it.next()
            val queryOrder: Order? = YunBi.getInstance().queryOrder(order.id.toString())
            queryOrder?.let {
                if (it.isWait) {
                    YunBi.getInstance().cancelOrder(it.id.toString())
                }
            }
        }
    }

    fun cancelAskOrder(askPrice: Double): Boolean {
        askOrders?.let {
            var order: Order? = YunBi.getInstance().queryOrder(it.id.toString())
            if (order?.isWait as Boolean && df.format(order.price.toDouble()) != df.format(askPrice)) {
                order = YunBi.getInstance().cancelOrder(order.id.toString())
                return true
            }
            return false
        }
        return true
    }

    fun downLowCost(price: Double, kLine: List<KLine.Data>): Boolean {
        return kLine.takeLast(4).all {
            it.close < price
        }
    }

    fun whichStrategy(): String = "当前策略：" + if (type == Strategy.Radical) "激进" else "温和"
    override fun onHandle(coinInfo: CoinInfo?, type: Dimension?, kLine: KLine?, tradesInfo: TradesInfo?, depthInfo: DepthInfo?, dealHandle: DealHandle?): Boolean {
        if (kLine?.datas == null || kLine.datas.isEmpty()) {
            return false
        }
        val bolls: ArrayList<Boll>? = Boll.create(kLine) as ArrayList<Boll>
        if (bolls == null || bolls.isEmpty()) {
            return false
        }

        val kdjs: ArrayList<Kdj>? = Kdj.create(kLine) as ArrayList<Kdj>
        if (kdjs == null || kdjs.isEmpty()) {
            return false
        }



        askOrders?.let {
            var order: Order? = YunBi.getInstance().queryOrder(askOrders?.id.toString())
            if (order?.isDone as Boolean) {
                val askMoney = (order?.price?.toDouble() ?: 0.0) * (order?.volume?.toDouble() ?: 0.0)
                val income = askMoney - bidMoney
                totalIncome += income
                System.err.println("$coinName 最新收益=$income")
                System.err.println("$coinName 总收益=" + totalIncome)
                MessageSend.sendNormalMessage(coinName + "\n${whichStrategy()}", cost.toString() + "\n买入数量：$bidAmount \n总成本：${df.format(bidMoney.toDouble())}", order?.price.toString() + "\n卖出数量：${order?.volume?.toDouble()} \n总收入：${df.format(askMoney)}", df.format(income.toString().toDouble()), df.format(totalIncome.toString().toDouble()))
                isBuy = false
                askOrders = null
                tradePosition = 0.0

                clearBidOrders()
            }
        }
        val currentPrice = kLine.datas.last().close
        //损失超过losepercent,就挂卖单
        val prime = getCost()
        if (prime.cost != 0.0 && downLowCost(prime.cost * (1 - losePercent), kLine.datas) && isBuy) {
            println("跌破$losePercent%")
            suspend = true
            cancelBidOrders()
            if (cancelAskOrder(currentPrice - diffPrice)) {
                val order: Order? = YunBi.getInstance().createOrder(market, (prime.amount).toString(), (currentPrice).toString(), false)
                order?.let { askOrders = order }
            }
        }
        val state = judgeState(kdjs, bolls, kLine, type)
        if (suspend) {
            println("当前处于暂停买单状态")
            return false
        }
        when (state) {
            State.None -> {
                if (bidOrders.isNotEmpty()) {
                    val it = bidOrders.iterator()
                    while (it.hasNext()) {
                        val next = it.next()
                        val queryOrder: Order? = YunBi.getInstance().queryOrder(next.id.toString())
                        if (queryOrder?.isCancelled ?: false) {
                            it.remove()
                        }

                    }
                }
            }
            State.Bid -> {

                //之前有没有挂买单，如果没有，直接挂买单，如果有，看之前的挂单情况，如果成功了，考虑是否加仓，如果没成功，取消之后挂新单
                if (bidOrders.isNotEmpty() && this.type == Strategy.MILD) {
                    val it = bidOrders.iterator()
                    val addOrders = arrayListOf<Order>()
                    while (it.hasNext()) {
                        val next = it.next()
                        var order: Order? = YunBi.getInstance().queryOrder(next.id.toString())
//                        if (order?.isDone as Boolean && !waitId.contains(order.id) && getBollMb(bolls) <= order.avgPrice.toDouble() / 0.998001) {
                        if (order?.isDone as Boolean && depthInfo?.bid?.first()?.price != order.price.toDouble()) {
                            val buyOrder: Order? = buy(0.5, depthInfo?.bid?.first()?.price ?: 0.0)
                            buyOrder?.let {
                                addOrders.add(it)
                            }
                        } else if (order.isWait) {
                            if (depthInfo?.bid?.first()?.price != order.price.toDouble()) {
                                order = YunBi.getInstance().cancelOrder(order.id.toString())
                                order?.let {
                                    val buyOrder: Order? = buy(0.5, depthInfo?.bid?.first()?.price ?: 0.0, true)
                                    buyOrder?.let {
                                        addOrders.add(it)
                                    }
                                }
                            }
                        }
                    }
                    bidOrders.addAll(addOrders)
                } else {
                    val buyOrder: Order? = buy(if (this.type == Strategy.Radical) 1.0 else 0.5, depthInfo?.bid?.first()?.price ?: 0.0)
                    buyOrder?.let {
                        bidOrders.add(it)
                    }

                }
            }
            State.ASK_NOW, State.Ask -> {
//                val it = bidOrders.iterator()
//                var totalPrice: Double = 0.0
//                var amount: Double = 0.0
//
//                while (it.hasNext()) {
//                    var order = it.next()
//                    order = YunBi.getInstance().queryOrder(order.id.toString())
//                    if (order.isDone) {
//                        isBuy = true
//                        totalPrice += order.price.toDouble() * order.volume.toDouble()
//                        amount += order.volume.toDouble()
//                    } else if (order.isWait) {
//                        YunBi.getInstance().cancelOrder(order.id.toString())
//                    }
//                }
                cancelBidOrders()
                val prime = getCost()

                if (!isBuy) {
                    return false
                }
                if (prime.amount == 0.0 || prime.cost == 0.0) {
                    return false
                }

                System.err.println("$coinName 平均成本=$cost  总价格=$bidMoney  总数量=$bidAmount")
                var isSell = true
                if (askOrders != null) {
                    val order: Order? = YunBi.getInstance().queryOrder(askOrders?.id.toString())
                    isSell = false
                    if (order?.isWait as Boolean ?: false && df.format(order.price.toDouble()) != df.format(depthInfo?.ask?.first()?.price)) {
                        var cancelOrder: Order? = YunBi.getInstance().cancelOrder(order.id.toString())
                        var i: Int = 0;
                        while (i <= 10) {
                            i++
                            cancelOrder = YunBi.getInstance().queryOrder(cancelOrder?.id.toString())
                            if (cancelOrder?.isCancelled ?: false) {
                                isSell = true
                                break
                            }
                        }
                    } else if (order.isCancelled ?: false) {
                        isSell = true
                    }
                }
                fun caculateSellPrice(): Double {
                    if (state == State.ASK_NOW) {
                        return Math.max(getSellPrice(), depthInfo?.ask?.first()?.price ?: 0.0)
                    } else {
                        //如果成本价和当前价格的误差在0.3%,可以暂缓卖出
                        val cost = getCost().cost
                        if (Math.abs(cost - currentPrice) / cost <= 0.003) {
                            return Math.max(getSellPrice(), depthInfo?.ask?.first()?.price ?: 0.0)
                        }
                        return depthInfo?.ask?.first()?.price ?: 0.0
                    }
                }
                if (isSell) {
                    val order:Order? = YunBi.getInstance().createOrder(market, (bidAmount * 0.999).toString(), caculateSellPrice().toString(), false)
                    order?.let { askOrders = order }
                }
            }
            State.CANCEL -> {
                cancelBidOrders()
            }
            State.CANCELASK -> {
                cancelAskOrder(0.0)

            }

        }



        return false
    }

    override fun onTestHandle(coinInfo: CoinInfo?, type: Dimension?, kLine: KLine?, tradesInfo: TradesInfo?, depthInfo: DepthInfo?): Boolean {

        if (kLine?.datas == null || kLine.datas.isEmpty()) {
            return false
        }

//        for (index in 1..kLine.datas.size - 2) {
//            println("${sdf.format(kLine.datas[index + 1].date * 1000)}" + CandleFactory.create(kLine.datas[index], kLine.datas[index + 1]))
//        }

        val bolls: ArrayList<Boll>? = Boll.create(kLine) as ArrayList<Boll>
        if (bolls == null || bolls.isEmpty()) {

            return false
        }
//        println(bolls)
        val kdjs: ArrayList<Kdj>? = Kdj.create(kLine) as ArrayList<Kdj>
//        println(kdjs)
        if (kdjs == null || kdjs.isEmpty()) {
            return false
        }


        val state = judgeTestState(kdjs, bolls, kLine, type)


        return false
    }


    fun chooseStrategy(bolls: ArrayList<Boll>, kline: List<KLine.Data>): Strategy {
        val boll = bolls.last()
        val currentPrice = kline.last().close
        if (currentPrice in boll.mb..boll.up) {
            return Strategy.Radical
        } else {
            return Strategy.MILD
        }
    }

    fun judgeState(kdjs: ArrayList<Kdj>, bolls: ArrayList<Boll>, kline: KLine, type: Dimension?): State {
        if (type == Dimension.MIN_15) {
            this.type = chooseStrategy(bolls, kline.datas)
            return State.None
        }

        val index = kline.datas.size - 1
        val currentPrice = kline.datas[index].close
        val prePrice = kline.datas[index - 1].close
        val befPrice = kline.datas[index - 2].low
        val (time, mb, up, dn) = bolls[index - 1]

        val width: Double = (up - dn) / mb
        println("$coinName 宽口=$width")
        if (width <= 0.01) {
            println("$coinName 宽口=$width,较小")
            if (!isBuy) {
                return State.None
            }
        }
        println("$coinName 均价$prePrice,mb=$mb,up=$up,dn=${dn}，成本=${getCost()}卖单价=${getCost().cost / 0.998},当前价格=$currentPrice")
        if (askOrders != null && prePrice < mb) {
            return State.CANCELASK
        } else if (kdjSignal(kdjs, bolls, kline.datas)) {
            println("$coinName 买点${Boll.formatTime(time)},买单价$prePrice,dn$dn,mb=$mb,up$up")
            return State.Bid
        } else if (this.type == Strategy.MILD && getSellPrice() < currentPrice) {
            println("高于成本的卖点$coinName 卖点${Boll.formatTime(time)},买单价$prePrice,up$up,mb=$mb,up$up")
            return State.ASK_NOW
        } else if (prePrice >= up) {
            suspend = false
            if (readSellPrice == 0.0) {
                readSellPrice = currentPrice
                return State.None
            } else if (readSellPrice > currentPrice) {
                println("$coinName 卖点${Boll.formatTime(time)},买单价$prePrice,up$up,mb=$mb,up$up")
                readSellPrice = 0.0
                return State.Ask
            } else if (readSellPrice < currentPrice) {
                readSellPrice = currentPrice
            }
            return State.None
        } else if (readSellPrice != 0.0) {
            readSellPrice = 0.0
            println("$coinName 低于上轨线后的卖点${Boll.formatTime(time)},买单价$prePrice,up$up,mb=$mb,up$up")
            return State.Ask
        } else if (prePrice >= mb) {

            return State.CANCEL
        } else if (isContinuouslyDown(bolls, kline)) {
            return State.None
        } else {
            return State.None
        }
    }


    fun getBollMb(bolls: ArrayList<Boll>): Double = bolls.last().mb
    fun getBollDn(bolls: ArrayList<Boll>): Double = bolls.last().dn


    fun judgeTestState(kdjs: ArrayList<Kdj>, bolls: ArrayList<Boll>, kline: KLine, type: Dimension?) {
        if (type == Dimension.MIN_15) {
            this.type = chooseStrategy(bolls, kline.datas)
            return
        }

//        val index = kline.datas.size - 1
        var isBuy = false
        val bidPrice = arrayListOf<Double>()
        var income: Double = 0.0

        for (index in 3..kline.datas.size - 1) {
            val currentPrice = kline.datas[index].close
            val prePrice = kline.datas[index - 1].low
            val (time, mb, up, dn) = bolls[index]

            val width: Double = (up - dn) / mb
//            println("宽口=$width")
            if (width <= 0.01) {
//                println("宽口=$width,较小")
                continue
//                return State.None
            }
//            println("宽口=$width 符合")
//            println("均价$currentPrice,mb=$mb,up=$up,dn=$dn")
            if (kdjSignal(kdjs.take(index), bolls.take(index), kline.datas.take(index))) {
                println("$coinName 买点${Boll.formatTime(time)},买单价$currentPrice,dn$dn,mb=$mb,up$up")
//                println("买点${Boll.formatTime(time)},买单价$currentPrice")
                bidPrice.add(currentPrice)
                isBuy = true
//                return State.Bid
            } else if (bidPrice.sum() / bidPrice.size / 0.997 < currentPrice) {
                println("$coinName 卖点${Boll.formatTime(time)},卖单价$currentPrice")
                isBuy = false
                income += currentPrice - (bidPrice.sum() / bidPrice.size)
                bidPrice.clear()
            } else if (prePrice + diffPrice >= up) {
                if (!isBuy) {
                    continue
                }
//                println("卖点${Boll.formatTime(time)},买单价$currentPrice,up$up,mb=$mb,up$up")
//                println("卖点${Boll.formatTime(time)},买单价$currentPrice")
//                return State.Ask
                if (readSellPrice == 0.0) {
                    readSellPrice = prePrice
//                    return State.None
                } else if (readSellPrice > currentPrice) {
                    readSellPrice = 0.0
                    println("$coinName 卖点${Boll.formatTime(time)},卖单价$currentPrice")
                    isBuy = false
                    income += currentPrice - (bidPrice.sum() / bidPrice.size)
                    bidPrice.clear()
//                    return State.Ask
                } else if (readSellPrice < currentPrice) {
                    readSellPrice = currentPrice
                }
            } else if (readSellPrice != 0.0) {
                readSellPrice = 0.0
                println("$coinName 低于上轨线后的卖点${Boll.formatTime(time)},买单价$prePrice,up$up,mb=$mb,up$up")
                println("$coinName 卖点${Boll.formatTime(time)},卖单价$currentPrice")
                isBuy = false
                income += currentPrice - (bidPrice.sum() / bidPrice.size)
                bidPrice.clear()
            } else if (currentPrice >= mb) {
//                if (!isBuy) {
//                    continue
//                }


//                if (readSellPrice == 0.0) {
//                    readSellPrice = currentPrice
////                    return State.None
//                } else if (readSellPrice > currentPrice) {
//                    readSellPrice = 0.0
//                    println("卖点${Boll.formatTime(time)},卖单价$currentPrice")
//                    isBuy = false
//                    income = currentPrice - bidPrice.sum() / bidPrice.size
////                    return State.Ask
//                } else if (readSellPrice < currentPrice) {
//                    readSellPrice = currentPrice
//                }
//                return State.None
//            } else if (isContinuouslyDown(bolls, kline)) {
//                return State.ASK_NOW
            } else {
//                return State.None
            }
        }
        println("总收益=" + income)
    }


    fun isContinuouslyDown(bolls: ArrayList<Boll>, kline: KLine): Boolean {
        for (i in kline.datas.size - 3..kline.datas.size - 1) {
            val boll = bolls[i].dn
            val current = kline.datas[i]
            if (current.low > boll - diffPrice) {
                return false
            }
        }
        return true
    }

    //

    /**
     * 在下轨线中判断该点是不是已经脱离了下轨线
     */
    fun chooseFirstPointAwayDn(kdjs: List<Kdj>, bolls: List<Boll>, kline: List<KLine.Data>): Boolean {
        //从前前一个点开始往前，在下轨线的个数
        var count = 0
        if (bolls[bolls.size - 1].dn in kline[kline.size - 1].low..kline[kline.size - 1].high) {
            return false
        }

        //如果前一个点在下轨线的话，不考虑
        if (bolls[bolls.size - 2].dn in kline[kline.size - 2].low..kline[kline.size - 2].high) {
            return false
        }
//        println("前两个点${Boll.formatTime(bolls[bolls.size - 3].time)}  ${bolls[bolls.size - 3].dn}  ${kline[kline.size - 3].low}..${kline[kline.size - 3].high}")
        val range = kline[kline.size - 3].low..kline[kline.size - 3].high
        if (bolls[bolls.size - 3].dn + diffPrice !in range && bolls[bolls.size - 3].dn !in range && bolls[bolls.size - 3].dn >= kline[kline.size - 3].high) {
            return false
        }

        for (i in bolls.size - 4 downTo 0) {
            if (bolls[i].dn !in kline[i].low..kline[i].high) {

                break
            }
//            if (bolls[i].mb in kline[i].low..kline[i].high) {
//                break
//            }
            count++
        }
        //当前点已经不是第二个脱离下轨线的点
        if (count == 0) {
            return false
        }
        //之前有5个点一直处在下轨线，有可能是瀑布，不考虑
        println("$coinName count${count}")
        if (count in 5..7) {
            return false
        }
        return true
    }

    fun kdjSignal(kdjs: List<Kdj>, bolls: List<Boll>, kline: List<KLine.Data>): Boolean {
        val kdj = kdjs[kdjs.size - 1]
        val preKdj = kdjs[kdjs.size - 2]
        val befKdj = kdjs[kdjs.size - 3]

        if (preKdj.d > 25 && Math.abs(preKdj.k - preKdj.d) / preKdj.k >= 0.1 && preKdj.k < preKdj.d) {
            return false
        }


        val boll = bolls[bolls.size - 1]
        val preBoll = bolls[bolls.size - 2]
        val befBoll = bolls[bolls.size - 3]

        val currentPrice = kline[kline.size - 1].close
        val prePrice = kline[kline.size - 2].close

//        println("" + currentPrice + "  " + prePrice)
//        println(befKdj.toString() + "\n" + preKdj)
//        println(befBoll.toString() + "\n" + preBoll)
//        if (currentPrice < (boll.mb - boll.dn) * 0.4 + boll.dn) {
        if (currentPrice < boll.mb) {
//            println(""+currentPrice + "  " + prePrice)
//            println(preKdj.toString() + "\n" + kdj)
//            println(preBoll.toString() + "\n" + boll)

            return chooseFirstPointAwayDn(kdjs, bolls, kline)
        }
        return false
    }

    fun parseTime(time: String): Long {
        val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
        df.timeZone = TimeZone.getTimeZone("UTC")
        return df.parse(time).time
    }

    fun isUp(bolls: ArrayList<Boll>): Boolean {
        var flag = true
        for (i in bolls.size - 5..bolls.size - 2) {
            if (bolls[i].mb > bolls[i + 1].mb
                    || bolls[i].up > bolls[i + 1].up
                    || bolls[i].dn > bolls[i + 1].dn) {
                flag = false
            }
        }
        return flag
    }

    fun buy(percent: Double, price: Double, force: Boolean = false): Order? {

        if (balance == 0.0 || price == 0.0) {
            return null
        }
        val currentMin = System.currentTimeMillis() / 1000 / 60
        if (currentMin in buyTime..buyTime + 30 && !force) {
            println("$coinName 半小时已经买过了")
            return null

        }
        if (tradePosition >= 1.0) {
            return null
        }
        tradePosition += percent
        buyTime = System.currentTimeMillis() / 1000 / 60

        val spendMoney: Double = percent * balance as Double
        val amount: Double = spendMoney / price
        return YunBi.getInstance().createOrder(market, amount.toString(), price.toString(), true)
    }
}