package yunbi.model;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import soso.net.ConversionException;
import soso.net.ConvertData;

import java.util.List;

/**
 * Created by zhoujia on 2017/6/16.
 */
public class Markets implements ConvertData<List<Markets>> {
    public String id;
    public String name;

    @Override
    public String toString() {
        return "Market{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                '}';
    }

    @Override
    public List<Markets> convert(JsonElement jsonElement) throws ConversionException {
        return new Gson().fromJson(jsonElement.getAsJsonArray(), new TypeToken<List<Markets>>() {
        }.getType());
    }
}
