package soso.model;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;
import soso.net.ConversionException;
import soso.net.ConvertData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhoujia on 2017/6/17.
 */
public class KLine implements ConvertData<KLine> {


    public boolean success;
    public int errorCode;
    public String error;
    public int rl;
    @SerializedName("data")
    private List<JsonArray> data1;
    public List<Data> datas;

    @Override
    public String toString() {
        return "KLine{" +
                "success=" + success +
                ", errorCode=" + errorCode +
                ", error='" + error + '\'' +
                ", rl=" + rl +
                ", data=" + datas +
                '}';
    }

    @Override
    public KLine convert(JsonElement jsonElement) throws ConversionException {
        return new Gson().fromJson(jsonElement.getAsJsonObject(), KLine.class);
    }

    public KLine getKLine() {
        datas = new ArrayList<>();
        for (JsonArray array : data1) {
            datas.add(new Data(array.get(0).getAsLong(), array.get(1).getAsDouble(), array.get(2).getAsDouble(), array.get(3).getAsDouble(), array.get(4).getAsDouble(), array.get(5).getAsDouble()));
        }
        return this;
    }

    public static class Data {
        public long date;
        /**
         * 开盘价
         */
        public double open;
        /**
         * 最高价
         */
        public double high;
        /**
         * 最低价
         */
        public double low;
        /**
         * 收盘价
         */
        public double close;
        /**
         * 量
         */
        public double amount;

        public Data(long date, double open, double high, double low, double close, double amount) {
            this.date = date;
            this.open = open;
            this.high = high;
            this.low = low;
            this.close = close;
            this.amount = amount;
        }

        @Override
        public String toString() {
            return "Data{" +
                    "date=" + date +
                    ", open=" + open +
                    ", high=" + high +
                    ", low=" + low +
                    ", close=" + close +
                    ", amount=" + amount +
                    '}';
        }
    }
}
