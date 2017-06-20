package soso.model;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;
import soso.net.ConversionException;
import soso.net.ConvertData;

import java.util.List;

/**
 * 成交量信息
 * Created by zhoujia on 2017/6/16.
 */
public class TradesInfo implements ConvertData<TradesInfo> {
    public int max;

    public List<Deal> deals;

    public String ds;

    @Override
    public TradesInfo convert(JsonElement jsonElement) throws ConversionException {
        return new Gson().fromJson(jsonElement.getAsJsonObject(), TradesInfo.class);
    }

    @Override
    public String toString() {
        return "TradesInfo{" +
                "max=" + max +
                ", deals=" + deals +
                ", ds='" + ds + '\'' +
                '}';
    }

    public static class Deal {
        /**
         * 成交时间
         */
        public long date;
        /**
         * 成交价
         */
        public double price;
        /**
         * 成交量
         */
        public double amount;
        @SerializedName("trade_type")
        public String tradeType;


        @Override
        public String toString() {
            return "Deal{" +
                    "date=" + date +
                    ", price=" + price +
                    ", amount=" + amount +
                    ", tradeType='" + tradeType + '\'' +
                    '}';
        }
    }

}
