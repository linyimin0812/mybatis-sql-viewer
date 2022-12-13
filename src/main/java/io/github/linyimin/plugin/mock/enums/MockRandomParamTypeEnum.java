package io.github.linyimin.plugin.mock.enums;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

/**
 * @author banzhe
 * @date 2022/11/29 22:47
 **/
public enum MockRandomParamTypeEnum {
    string("string"),
    name("string"),
    datetime("datetime"),
    integer("integer"),
    decimal("decimal"),
    date("date"),
    timestamp("timestamp"),
    time("time"),
    year("year"),
    city("string"),
    url("string"),
    email("string"),
    ip("string"),
    university("string"),
    phone("string");

    private final String value;

    public String getValue() {
        return this.value;
    }


    MockRandomParamTypeEnum(String value) {
        this.value = value;
    }

    public static MockRandomParamTypeEnum resolve(String type) {
        if (StringUtils.isBlank(type)) {
            return string;
        }

        return Arrays.stream(MockRandomParamTypeEnum.values())
                .filter(randomType -> StringUtils.equals(type, randomType.name()))
                .findFirst()
                .orElse(string);
    }
}
