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
    city,
    url,
    email,
    ip,
    integer,
    decimal,
    university,
    date,
    timestamp,
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
