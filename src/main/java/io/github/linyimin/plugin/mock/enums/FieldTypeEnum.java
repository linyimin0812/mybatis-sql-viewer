package io.github.linyimin.plugin.mock.enums;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

/**
 * @author banzhe
 * @date 2022/12/02 13:27
 **/
public enum FieldTypeEnum {

    TINYINT(1),
    SMALLINT(2),
    MEDIUMINT(3),
    INT(4),
    BIGINT(8),
    FLOAT(4),
    DOUBLE(8),
    DECIMAL(-1),
    DATE(3),
    TIME(3),
    YEAR(1),
    DATETIME(8),
    TIMESTAMP(4),
    CHAR(-1),
    VARCHAR(-1),
    TINYTEXT(-1),
    MEDIUMTEXT(-1),
    LONGTEXT(-1),
    TINYBLOB(-1),
    MEDIUMBLOB(-1),
    BLOB(-1),
    LONGBLOB(-1),
    BINARY(-1),
    VARBINARY(-1);

    private int length;

    FieldTypeEnum(int length) {
        this.length = length;
    }

    public int getLength() {
        return this.length;
    }

    public static FieldTypeEnum resolve(String type) {

        if (StringUtils.isBlank(type)) {
            return FieldTypeEnum.VARCHAR;
        }

        return Arrays.stream(FieldTypeEnum.values()).filter(fieldType -> StringUtils.upperCase(type)
                .contains(fieldType.name()))
                .findFirst()
                .orElse(VARCHAR);
    }

}
