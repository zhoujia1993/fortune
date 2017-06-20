package soso.net;

import com.google.gson.JsonElement;

/**
 * Created by zhoujia on 2017/6/16.
 */
public interface ConvertData<D> {
    D convert(JsonElement jsonElement) throws ConversionException;
}
