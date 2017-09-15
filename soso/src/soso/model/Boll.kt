package soso.model

import java.text.DecimalFormat
import java.text.SimpleDateFormat

/**
 * Created by zhoujia on 2017/7/22.
 */
data class Boll(val time: Long, val mb: Double, val up: Double, val dn: Double) {


    private var sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    fun formatTime(): String = sdf.format(time)
    override fun toString(): String {
        return "Boll(time=${formatTime()}, mb=$mb, up=$up, dn=$dn)\n"
    }


    companion object {
        private val df = DecimalFormat(".00001")
        fun formatTime(boll: Boll): String = formatTime(boll.time)
        fun formatTime(time: Long): String = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(time)


        fun create(kLine: KLine): List<Boll>? {
            val range = kLine.datas?.indices
            if (range != null) {
                var bolls: ArrayList<Boll> = arrayListOf()
                var mb: Double
                var up: Double
                var dn: Double
                var time: Long
                for (i in range) {
                    if (i == 0) {
                        mb = kLine.datas[0].close
                        up = mb
                        dn = mb
                        time = kLine.datas[0].date * 1000
                    } else {
                        val count: Int = Math.min(i, 19) + 1
                        val pre: Int = Math.max(0, i - 20)
                        var diffPrice: Double = 0.0
                        val sumPrice: Double = (pre..(pre + count - 1)).sumByDouble { kLine.datas[it].close }
                        val ma: Double = sumPrice.div(count)
                        for (j in pre..(pre + count - 1)) {
                            diffPrice += Math.pow(kLine.datas[j].close - ma, 2.0)
                        }
                        val md: Double = Math.sqrt(diffPrice.div(count))
//                        mb = if (count != 1) sumPrice.minus(kLine.datas?.get(pre + count - 1)?.close as Double).div(count - 1) else kLine.datas[0].close
                        mb = df.format(ma).toDouble()
                        up = df.format(mb + 2 * md).toDouble()
                        dn = df.format(mb - 2 * md).toDouble()
                        time = kLine.datas[i].date * 1000
                    }
                    val boll = Boll(time, mb, up, dn)
//                    println(boll)
                    bolls.add(boll)

                }
                return bolls
            } else {
                return null
            }
        }
    }

}


