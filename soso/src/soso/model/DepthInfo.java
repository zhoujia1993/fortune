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
public class DepthInfo implements ConvertData<DepthInfo> {

    public int max;
    @SerializedName("asks")
    private List<JsonArray> data1;
    @SerializedName("bids")
    private List<JsonArray> data2;
    /**
     * 现在的时间
     */
    public String now;
    public String result;
    public int length;
    public String ds;
    /**
     * 卖方，按照顺序由卖一开始往后
     */
    public List<Data> ask;
    /**
     * 买方，按照顺序有买一开始往后
     */
    public List<Data> bid;

    @Override
    public DepthInfo convert(JsonElement jsonElement) throws ConversionException {
        return new Gson().fromJson(jsonElement.getAsJsonObject(), DepthInfo.class);
    }

    public DepthInfo getDepthInfo() {
        ask = new ArrayList<>();
        bid = new ArrayList<>();
        for (JsonArray array : data1) {
            Data data = new Data(array.get(0).getAsDouble(), array.get(1).getAsDouble());
            ask.add(data);
        }
        for (JsonArray array : data2) {
            Data data = new Data(array.get(0).getAsDouble(), array.get(1).getAsDouble());
            bid.add(data);
        }
        return this;
    }

    @Override
    public String toString() {
        return "DepthInfo{" +
                "max=" + max +
                ", now='" + now + '\'' +
                ", result='" + result + '\'' +
                ", length=" + length +
                ", ds='" + ds + '\'' +
                ", asks=" + ask +
                ", bids=" + bid +
                '}';
    }

    public static class Data {
        /**
         * 出价
         */
        public double price;
        /**
         * 数量
         */
        public double amount;

        public Data(double price, double amount) {
            this.price = price;
            this.amount = amount;
        }

        @Override
        public String toString() {
            return "Data{" +
                    "price=" + price +
                    ", amount=" + amount +
                    '}';
        }
    }
}
