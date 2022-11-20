package io.github.linyimin.plugin.mybatis.parsing;

/**
 * @author Clinton Begin
 **/
public class ParseException extends RuntimeException {
    private static final long serialVersionUID = 3880206998166270511L;

    public ParseException() {
        super();
    }

    public ParseException(String message) {
        super(message);
    }

    public ParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public ParseException(Throwable cause) {
        super(cause);
    }
}
