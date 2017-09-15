package soso.model

/**
 * Created by zhoujia on 2017/7/23.
 */
data class Kdj(val k: Double, val d: Double, val j: Double) {
    companion object {


        fun create(kLine: KLine): List<Kdj> {
            var k: Double = 0.0
            var d: Double = 0.0
            var j: Double = 0.0
            val kdjs = arrayListOf<Kdj>()
            for (i in kLine.datas.indices) {
                if (i == 0) {
                    val day0 = kLine.datas[0]
                    val rsv:Double = if(day0.high!=day0.low) (day0.close - day0.low) * 100 / (day0.high - day0.low) else 0.0
                    k = (rsv + 2 * 50) / 3
                    d = (k + 2 * 50) / 3
                    j = 3 * k - 2 * d
                    kdjs.add(Kdj(k, d, j))
                } else {
                    val close = kLine.datas[i].close
                    val day = Math.min(i, 8)
                    var min: Double = Double.MAX_VALUE
                    var max: Double = Double.MIN_VALUE
                    for (m in i - day..i) {
                        val minPrice = kLine.datas[m].low
                        val maxPrice = kLine.datas[m].high
                        if (min > minPrice) {
                            min = minPrice
                        }
                        if (max < maxPrice) {
                            max = maxPrice
                        }
                    }
                    val rsv:Double  = if (max != min) (close - min) / (max - min) * 100 else 0.0
                    val (pk, pd, pj) = kdjs[i - 1]
                    k = (rsv + 2 * pk) / 3
                    d = (k + 2 * pd) / 3
                    j = 3 * k - 2 * d
                    kdjs.add(Kdj(k, d, j))
                }
            }
            return kdjs
        }
    }
}

