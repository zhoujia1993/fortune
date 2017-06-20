package soso.net;

/**
 * Created by zhoujia on 2017/6/16.
 */
public class ConversionException extends Exception {
    public ConversionException(String message) {
        super(message);
    }

    public ConversionException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public ConversionException(Throwable throwable) {
        super(throwable);
    }
}

