package soso.candlestickCharts

import soso.model.KLine
import java.text.SimpleDateFormat

/**
 * Created by zhoujia on 2017/7/25.
 */

object CandleFactory {
    private val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

    enum class Type {
        RED, GREEN, WHITE, NONE,
        ERED, SRED, MRED, BRED,
        EGREEN, SGREEN, MGREEN, BGREEN
    }

    fun create(preData: KLine.Data, data: KLine.Data): Type {
        val weight = range(preData, data)
        if (weight > 0.004 && weight < 0.006) {
            return Type.ERED
        } else if (weight >= 0.006 && weight < 0.015) {
            return Type.SRED
        } else if (weight >= 0.015 && weight < 0.035) {
            return Type.MRED
        } else if (weight >= 0.035) {
            return Type.BRED
        } else if (weight > -0.006 && weight < -0.004) {
            return Type.EGREEN
        } else if (weight > -0.015 && weight <= -0.006) {
            return Type.SGREEN
        } else if (weight > -0.035 && weight <= 0.015) {
            return Type.MGREEN
        } else if (weight <= -0.035) {
            return Type.BGREEN
        }
        return Type.NONE
    }

    fun type(data: KLine.Data?): Type {
        if (data == null) {
            return Type.NONE
        }
        if (data.open < data.close) {
            return Type.RED
        } else if (data.open > data.close) {
            return Type.GREEN
        } else {
            return Type.WHITE
        }
    }

    fun upWeight(data: KLine.Data?): Double {
        if (data == null || data.high == data.low) {
            return 0.0
        }
        //红或十字星
        if (data.open <= data.close) {
            return (data.high - data.close) / (data.high - data.low)
            //绿
        } else {
            return (data.high - data.open) / (data.high - data.low)
        }
    }

    fun entityWeight(data: KLine.Data?): Double {
        if (data == null || data.high == data.low) {
            return 0.0
        }
        return Math.abs(data.open - data.close) / (data.high - data.low)
    }

    fun downWeight(data: KLine.Data?): Double {
        if (data == null || data.high == data.low) {
            return 0.0
        }
        //红或十字星
        if (data.open <= data.close) {
            return (data.open - data.low) / (data.high - data.low)
            //绿
        } else {
            return (data.close - data.low) / (data.high - data.low)
        }
    }

    fun range(preData: KLine.Data?, data: KLine.Data?): Double {
        if (data == null || preData == null) {
            return 0.0
        }
        return (data.close - data.open) / preData.close
    }

}
