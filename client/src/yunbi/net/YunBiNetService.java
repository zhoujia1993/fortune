package yunbi.net;

import soso.net.Method;
import soso.net.NameValuePair;
import soso.net.NetService;
import yunbi.Utils;

import java.lang.reflect.Type;

/**
 * Created by zhoujia on 2017/6/16.
 */
public class YunBiNetService<T> extends NetService<T> {

//    @Override
//    public String handleUrl(Method method, String url, NameValuePair pair, boolean signature) {
//
//        return url;
//    }


//    @Override
//    public T request(Method method, String url, NameValuePair pair, Type type) {
//        return super.request(method, url, pair, type);
//    }

    public T request(Method method, String url, NameValuePair pair, boolean signature, Type type) {
        if (Utils.isEmpty(url)) {
            return null;
        }
        String encrypt = null;
        String params = null;
        if (pair != null) {
            params = pair.buildParams();
            if (signature) {
                encrypt = method + "|" + url + "|" + params;
                encrypt = Utils.encrypt(encrypt);

            }
        }
        if (url.startsWith("/api/v2")) {
            url = "https://yunbi.com" + url;
        }
        if (method == Method.GET) {
            boolean flag = false;
            if (!Utils.isEmpty(params)) {
                url = url + "?" + params;
                flag = true;
            }
            if (signature && !Utils.isEmpty(encrypt)) {
                if (!flag) {
                    url = url + "?";
                }
                url = url + "&signature=" + encrypt;
            }
        } else if (method == Method.POST) {
            if (pair != null) {
                pair.put("signature", encrypt);
            }
        }
        return super.request(method, url, pair, type);
    }

}
