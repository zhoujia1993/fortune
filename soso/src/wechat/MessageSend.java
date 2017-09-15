package wechat;

import soso.Config;
import soso.net.JsonParam;
import soso.net.Method;
import soso.net.NetService;

import java.text.SimpleDateFormat;

/**
 * Created by zhoujia on 2017/7/21.
 */
public class MessageSend {
    private static final String ACCESS_TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=" + Config.WECHAT_APPID + "&secret=" + Config.WECHAT_SECRET_KEY;
    private static final String URL = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=";
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static void sendNormalMessage(String coin, String bid, String ask, String in, String totalIncome) {
        Token token = new NetService<Token>().request(Method.GET, ACCESS_TOKEN_URL, null, Token.class);
        if (token == null) {
            return;
        }
        Value coinName = new Value(coin, "#173177");
        Value time = new Value(sdf.format(System.currentTimeMillis()), "#173177");
        Value bidPrice = new Value(bid, "#173177");
        Value askPrice = new Value(ask, "#173177");
        Value income = new Value(in, "#173177");
        Value totalIn = new Value(totalIncome, "#173177");
        JsonParam jsonParam = new JsonParam();
        jsonParam.put(new TemplateMessage(Config.WECHAT_TO_USER, Config.WECHAT_TEMPLATE1, "#173177", new Data1(coinName, time, bidPrice, askPrice, income, totalIn)));
        Object result = new NetService<Object>().request(Method.POST, URL + token.access_token, jsonParam, Object.class);
//        System.out.println(result);
    }

    public static void sendGuaMessage(String coin, String text) {
        Token token = new NetService<Token>().request(Method.GET, ACCESS_TOKEN_URL, null, Token.class);
        if (token == null) {
            return;
        }
        Value coinName = new Value(coin, "#173177");
        Value time = new Value(sdf.format(System.currentTimeMillis()), "#173177");
        Value content = new Value(text, "#173177");
        JsonParam jsonParam = new JsonParam();
        jsonParam.put(new TemplateMessage(Config.WECHAT_TO_USER, Config.WECHAT_TEMPLATE2, "#173177", new Data2(coinName, time, content)));
        Object result = new NetService<Object>().request(Method.POST, URL + token.access_token, jsonParam, Object.class);
        System.out.println(result);
    }

    public static void sendlossMessage(String coin, String percent, String currentPrice, String bidPrice, String askPrice) {
        Token token = new NetService<Token>().request(Method.GET, ACCESS_TOKEN_URL, null, Token.class);
        if (token == null) {
            return;
        }
        Value coinName = new Value(coin, "#173177");
        Value time = new Value(sdf.format(System.currentTimeMillis()), "#173177");

        Value per = new Value(percent, "#173177");
        Value cPrice = new Value(currentPrice, "#173177");
        Value bPrice = new Value(bidPrice, "#173177");
        Value aPrice = new Value(askPrice, "#173177");


        JsonParam jsonParam = new JsonParam();
        jsonParam.put(new TemplateMessage(Config.WECHAT_TO_USER, Config.WECHAT_TEMPLATE3, "#173177", new Data3(coinName, time, per, cPrice, bPrice, aPrice)));
        Object result = new NetService<Object>().request(Method.POST, URL + token.access_token, jsonParam, Object.class);
        System.out.println(result);
    }


    public static class TemplateMessage {
        public String touser;
        public String template_id;
        public String topcolor;
        public Data data;

        public TemplateMessage(String touser, String template_id, String topcolor, Data data) {
            this.touser = touser;
            this.template_id = template_id;
            this.topcolor = topcolor;
            this.data = data;
        }
    }


    public static abstract class Data {

    }

    public static class Data1 extends Data {

        public Value coinName;
        public Value time;
        public Value bidPrice;
        public Value askPrice;
        public Value income;
        public Value totalIncome;


        public Data1(Value coinName, Value time, Value bidPrice, Value askPrice, Value income, Value totalIncome) {
            this.coinName = coinName;
            this.time = time;
            this.bidPrice = bidPrice;
            this.askPrice = askPrice;
            this.income = income;
            this.totalIncome = totalIncome;
        }
    }

    public static class Data2 extends Data {
        public Value coinName;
        public Value time;
        public Value content;

        public Data2(Value coinName, Value time, Value content) {
            this.coinName = coinName;
            this.time = time;
            this.content = content;
        }
    }

    public static class Data3 extends Data {

        public Value coinName;
        public Value time;
        public Value percent;
        public Value currentPrice;
        public Value bidPrice;
        public Value askPrice;


        public Data3(Value coinName, Value time, Value percent, Value currentPrice, Value bidPrice, Value askPrice) {
            this.coinName = coinName;
            this.time = time;
            this.percent = percent;
            this.currentPrice = currentPrice;
            this.bidPrice = bidPrice;
            this.askPrice = askPrice;
        }
    }

    public static class Value {
        public String value;
        public String color;

        public Value(String value, String color) {
            this.value = value;
            this.color = color;
        }
    }

    //    {"access_token":"ACCESS_TOKEN","expires_in":7200}
    public static class Token {
        public String access_token;
        public int expires_in;
    }
}
