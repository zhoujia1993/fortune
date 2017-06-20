package soso.net;

import com.google.gson.Gson;

/**
 * Created by zhoujia on 2017/6/17.
 */
public class JsonParam implements Param {
    private String params;

    public JsonParam() {
    }

    public void put(Object o) {
        this.params = new Gson().toJson(o);
    }

    @Override
    public String buildParams() {
        return params;
    }
}
