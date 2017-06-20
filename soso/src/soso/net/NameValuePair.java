package soso.net;

/**
 * Created by zhoujia on 2017/6/16.
 */
public class NameValuePair implements Param {
    private StringBuilder stringBuilder;

    public NameValuePair() {
        this.stringBuilder = new StringBuilder();
    }

    public NameValuePair put(String key, String value) {
        stringBuilder.append(key);
        stringBuilder.append("=");
        stringBuilder.append(value);
        stringBuilder.append("&");
        return this;
    }

    @Override
    public String buildParams() {
        return stringBuilder.toString().substring(0, stringBuilder.toString().length() - 1);
    }
}
