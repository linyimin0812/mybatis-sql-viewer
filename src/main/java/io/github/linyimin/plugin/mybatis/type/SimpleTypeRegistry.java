package io.github.linyimin.plugin.mybatis.type;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Clinton Begin
 */
public class SimpleTypeRegistry {

    private static final Set<Class<?>> SIMPLE_TYPE_SET = new HashSet<>();
    private static final Set<String> SIMPLE_TYPE_STR_SET = new HashSet<>();

    static {
        SIMPLE_TYPE_SET.add(String.class);
        SIMPLE_TYPE_STR_SET.add("java.lang.String");

        SIMPLE_TYPE_SET.add(Byte.class);
        SIMPLE_TYPE_STR_SET.add("java.lang.Byte");

        SIMPLE_TYPE_SET.add(byte.class);
        SIMPLE_TYPE_STR_SET.add("byte");

        SIMPLE_TYPE_SET.add(Short.class);
        SIMPLE_TYPE_STR_SET.add("java.lang.Short");

        SIMPLE_TYPE_SET.add(short.class);
        SIMPLE_TYPE_STR_SET.add("short");

        SIMPLE_TYPE_SET.add(Character.class);
        SIMPLE_TYPE_STR_SET.add("java.lang.Character");

        SIMPLE_TYPE_SET.add(char.class);
        SIMPLE_TYPE_STR_SET.add("char");

        SIMPLE_TYPE_SET.add(Integer.class);
        SIMPLE_TYPE_STR_SET.add("java.lang.Integer");

        SIMPLE_TYPE_SET.add(int.class);
        SIMPLE_TYPE_STR_SET.add("int");

        SIMPLE_TYPE_SET.add(Long.class);
        SIMPLE_TYPE_STR_SET.add("java.lang.Long");

        SIMPLE_TYPE_SET.add(long.class);
        SIMPLE_TYPE_STR_SET.add("long");

        SIMPLE_TYPE_SET.add(Float.class);
        SIMPLE_TYPE_STR_SET.add("java.lang.Float");

        SIMPLE_TYPE_SET.add(float.class);
        SIMPLE_TYPE_STR_SET.add("float");

        SIMPLE_TYPE_SET.add(Double.class);
        SIMPLE_TYPE_STR_SET.add("java.lang.Double");

        SIMPLE_TYPE_SET.add(double.class);
        SIMPLE_TYPE_STR_SET.add("double");

        SIMPLE_TYPE_SET.add(Boolean.class);
        SIMPLE_TYPE_STR_SET.add("java.lang.Boolean");

        SIMPLE_TYPE_SET.add(boolean.class);
        SIMPLE_TYPE_STR_SET.add("boolean");

        SIMPLE_TYPE_SET.add(Date.class);
        SIMPLE_TYPE_STR_SET.add("java.util.Date");

        SIMPLE_TYPE_SET.add(BigInteger.class);
        SIMPLE_TYPE_STR_SET.add("java.math.BigInteger");

        SIMPLE_TYPE_SET.add(BigDecimal.class);
        SIMPLE_TYPE_STR_SET.add("java.math.BigDecimal");
    }

    private SimpleTypeRegistry() {
        // Prevent Instantiation
    }

    /*
     * Tells us if the class passed in is a known common type
     *
     * @param clazz The class to check
     * @return True if the class is known
     */
    public static boolean isSimpleType(Class<?> clazz) {
        return SIMPLE_TYPE_SET.contains(clazz);
    }

    public static boolean isSimpleType(String clazz) {
        return SIMPLE_TYPE_STR_SET.contains(clazz);
    }

}