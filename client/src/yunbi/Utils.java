package yunbi;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

/**
 * Created by zhoujia on 2017/6/16.
 */
public class Utils {
    public static boolean isEmpty(String text) {
        return text == null || text.length() == 0;
    }

    public static boolean isCollectionsEmpty(Map map) {
        return map == null || map.size() == 0;
    }

    public static String encrypt(String data) {

        try {
            SecretKeySpec signingKey = new SecretKeySpec(Config.SERCET_KEY.getBytes(), "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(signingKey);
            return new BigInteger(1, (mac.doFinal(data.getBytes()))).toString(16);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        String time = String.valueOf(System.currentTimeMillis());
        System.out.println(time);
        System.out.print(encrypt("POST|/api/v2/order/delete.json|access_key=aW6gCXN6J8NTjUx3MZz81vOzV8x8M4eupNYHD5Bd&id=436046325&tonce=" + time));
    }
}
