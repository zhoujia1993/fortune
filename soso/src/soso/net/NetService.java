package soso.net;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.internal.$Gson$Types;
import soso.Utils;

import java.io.*;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by zhoujia on 2017/6/16.
 */
public class NetService<T> {

    public T request(Method method, String url, Param pair, Type type) {
        if (Utils.isEmpty(url)) {
            return null;
        }

        try {
            URL url1 = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) url1.openConnection();
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(60000);
            connection.setRequestMethod(method.getMethod());
            connection.setDoInput(true);
            if (method == Method.POST && pair != null) {
                connection.setDoOutput(true);
                OutputStream os = connection.getOutputStream();
                os.write(pair.buildParams().getBytes());
                os.flush();
                os.close();
            }
            connection.connect();
            if (connection.getResponseCode() / 100 == 2) {
                InputStream is = connection.getInputStream();
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                int len = 0;
                byte[] bytes = new byte[1024];
                while ((len = is.read(bytes)) != -1) {
                    bos.write(bytes, 0, len);
                }
                BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bos.toByteArray())));
                String data = bos.toString();
                JsonElement rootElement;
                try {
                    rootElement = new JsonParser().parse(data);
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
                Class cls = getConvertDataClass(type);
                if (cls != null) {
                    try {
                        java.lang.reflect.Method convertMethod = cls.getDeclaredMethod("convert", JsonElement.class);
                        return (T) convertMethod.invoke(cls.newInstance(), rootElement);
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                }
                try {
                    return convertData(rootElement, type);
                } catch (RuntimeException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("请求返回码=" + connection.getResponseCode());
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String convert2params(Map<String, String> map) {
        if (Utils.isEmpty(map)) {
            return "";
        }
        Set<Map.Entry<String, String>> sets = map.entrySet();
        Iterator<Map.Entry<String, String>> its = sets.iterator();
        StringBuilder params = new StringBuilder();
        while (its.hasNext()) {
            Map.Entry<String, String> entry = its.next();
            params.append(entry.getKey());
            params.append("=");
            params.append(entry.getValue());
            params.append("&");
        }
        return params.toString().substring(0, params.toString().length() - 1);
    }

    private static Class getConvertDataClass(java.lang.reflect.Type type) {
        Class<?> cls = $Gson$Types.getRawType(type);
        //要对list等类型进行判断
        if (type instanceof ParameterizedType) {
            java.lang.reflect.Type[] types = ((ParameterizedType) type).getActualTypeArguments();
            if (types.length > 0) {
                for (java.lang.reflect.Type tmpType : types) {
                    Class tmpClass = $Gson$Types.getRawType(tmpType);
                    if (ConvertData.class.isAssignableFrom(tmpClass)) {
                        return tmpClass;
                    }
                }
            }
        }
        if (ConvertData.class.isAssignableFrom(cls)) {
            return cls;
        }
        return null;
    }

    private T convertData(JsonElement rootElement, Type type) {
        return new Gson().fromJson(rootElement, type);
    }

}
