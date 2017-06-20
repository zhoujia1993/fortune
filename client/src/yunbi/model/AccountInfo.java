package yunbi.model;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import soso.net.ConversionException;
import soso.net.ConvertData;

import java.util.List;

/**
 * Created by zhoujia on 2017/6/16.
 */
public class AccountInfo implements ConvertData<AccountInfo> {

    @Override
    public AccountInfo convert(JsonElement jsonElement) throws ConversionException {
        return new Gson().fromJson(jsonElement.getAsJsonObject(), AccountInfo.class);
    }

    public String sn;
    public String name;
    public String email;
    public String activated;
    public String memo;
    public List<Account> accounts;


    public static class Account {
        public String currency;
        public String balance;
        public String locked;
    }
}
