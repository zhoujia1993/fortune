package soso.net;

/**
 * Created by zhoujia on 2017/6/16.
 */
public enum Method {
    GET("GET"),
    POST("POST");

    private String method;

    private Method(String method) {
        this.method = method;
    }

    public String getMethod() {
        return method;
    }
}
