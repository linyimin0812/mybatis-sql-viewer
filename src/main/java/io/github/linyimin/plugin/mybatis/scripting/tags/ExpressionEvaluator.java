package io.github.linyimin.plugin.mybatis.scripting.tags;

import io.github.linyimin.plugin.mybatis.parsing.ParseException;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Clinton Begin
 */
public class ExpressionEvaluator {

    public boolean evaluateBoolean(String expression, Object parameterObject) {
        Object value = OgnlCache.getValue(expression, parameterObject);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof Number) {
            return new BigDecimal(String.valueOf(value)).compareTo(BigDecimal.ZERO) != 0;
        }
        return value != null;
    }

    /**
     * @since 3.5.9
     */
    public Iterable<?> evaluateIterable(String expression, Object parameterObject, boolean nullable) {
        Object value = OgnlCache.getValue(expression, parameterObject);
        if (value == null) {
            if (nullable) {
                return null;
            } else {
                throw new ParseException("The expression '" + expression + "' evaluated to a null value.");
            }
        }
        if (value instanceof Iterable) {
            return (Iterable<?>) value;
        }
        if (value.getClass().isArray()) {
            // the array may be primitive, so Arrays.asList() may throw
            // a ClassCastException (issue 209).  Do the work manually
            // Curse primitives! :) (JGB)
            int size = Array.getLength(value);
            List<Object> answer = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                Object o = Array.get(value, i);
                answer.add(o);
            }
            return answer;
        }
        if (value instanceof Map) {
            return ((Map) value).entrySet();
        }
        throw new ParseException("Error evaluating expression '" + expression + "'.  Return value (" + value + ") was not iterable.");
    }

}
