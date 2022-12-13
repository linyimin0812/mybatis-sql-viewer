package io.github.linyimin.plugin.mock.enums;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

/**
 * @author banzhe
 * @date 2022/12/02 13:27
 **/
public enum FieldTypeEnum {

    TINYINT(1, MockRandomParamTypeEnum.integer),
    SMALLINT(2, MockRandomParamTypeEnum.integer),
    MEDIUMINT(3, MockRandomParamTypeEnum.integer),
    INT(4, MockRandomParamTypeEnum.integer),
    BIGINT(8, MockRandomParamTypeEnum.integer),
    FLOAT(4, MockRandomParamTypeEnum.decimal),
    DOUBLE(8, MockRandomParamTypeEnum.decimal),
    DECIMAL(-1, MockRandomParamTypeEnum.decimal),
    DATE(3, MockRandomParamTypeEnum.date),
    TIME(3, MockRandomParamTypeEnum.time),
    YEAR(1, MockRandomParamTypeEnum.year),
    DATETIME(8, MockRandomParamTypeEnum.datetime),
    TIMESTAMP(4, MockRandomParamTypeEnum.timestamp),
    CHAR(-1, MockRandomParamTypeEnum.string),
    VARCHAR(-1, MockRandomParamTypeEnum.string),
    TINYTEXT(-1, MockRandomParamTypeEnum.string),
    MEDIUMTEXT(-1, MockRandomParamTypeEnum.string),
    LONGTEXT(-1, MockRandomParamTypeEnum.string),
    TINYBLOB(-1, MockRandomParamTypeEnum.string),
    MEDIUMBLOB(-1, MockRandomParamTypeEnum.string),
    BLOB(-1, MockRandomParamTypeEnum.string),
    LONGBLOB(-1, MockRandomParamTypeEnum.string),
    BINARY(-1, MockRandomParamTypeEnum.string),
    VARBINARY(-1, MockRandomParamTypeEnum.string);

    private final int length;
    private final MockRandomParamTypeEnum mockType;

    FieldTypeEnum(int length, MockRandomParamTypeEnum mockType) {
        this.length = length;
        this.mockType = mockType;
    }

    public int getLength() {
        return this.length;
    }
    public MockRandomParamTypeEnum getMockType() {
        return this.mockType;
    }

    public static FieldTypeEnum resolve(String type) {

        if (StringUtils.isBlank(type)) {
            return FieldTypeEnum.VARCHAR;
        }

        if (StringUtils.containsIgnoreCase(type, "unsigned")) {
            type = StringUtils.split(type, " ")[0];
        }

        String finalType = type;

        return Arrays.stream(FieldTypeEnum.values())
                .filter(fieldType -> StringUtils.upperCase(finalType).equals(fieldType.name()))
                .findFirst()
                .orElse(VARCHAR);
    }

}
