package yunbi.model;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;
import soso.net.ConversionException;
import soso.net.ConvertData;
import soso.strategydeal.DealHandle;

/**
 * Created by zhoujia on 2017/6/17.
 */
public class Order implements ConvertData<Order> {

    public long id;
    public String side;
    @SerializedName("ord_type")
    public String orderType;
    public String price;
    @SerializedName("avg_price")
    public String avgPrice;
    /**
     * 订单状态,wait,cancel,done
     */
    public String state;

    public String market;
    @SerializedName("created_at")
    public String createAt;
    public String volume;
    @SerializedName("remaining_volume")
    public String remainVolume;
    @SerializedName("executed_volume")
    public String executedVolume;
    @SerializedName("trades_count")
    public int tradesCount;

    @Override
    public Order convert(JsonElement jsonElement) throws ConversionException {
        return new Gson().fromJson(jsonElement.getAsJsonObject(), Order.class);
    }

    public boolean isWait() {
        return "wait".equals(state);
    }

    public boolean isDone() {
        return "done".equals(state);
    }

    public boolean isCancelled() {
        return "cancel".equals(state);
    }

    public int getState() {
        if (isWait()) {
            return DealHandle.STATE_WAIT;
        } else if (isDone()) {
            return DealHandle.STATE_DONE;
        } else if (isCancelled()) {
            return DealHandle.STATE_CANCEL;
        } else {
            return DealHandle.STATE_UNKNOWN;
        }
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", side='" + side + '\'' +
                ", orderType='" + orderType + '\'' +
                ", price='" + price + '\'' +
                ", avgPrice='" + avgPrice + '\'' +
                ", state='" + state + '\'' +
                ", market='" + market + '\'' +
                ", createAt='" + createAt + '\'' +
                ", volume='" + volume + '\'' +
                ", remainVolume='" + remainVolume + '\'' +
                ", executedVolume='" + executedVolume + '\'' +
                ", tradesCount=" + tradesCount +
                '}';
    }
}
