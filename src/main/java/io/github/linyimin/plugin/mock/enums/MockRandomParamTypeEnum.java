package io.github.linyimin.plugin.mock.enums;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

/**
 * @author banzhe
 * @date 2022/11/29 22:47
 **/
public enum MockRandomParamTypeEnum {
    string,
    name,
    date,
    timestamp,
    integer,
    decimal,
    city,
    url,
    email,
    ip,
    university,
    phone;

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
