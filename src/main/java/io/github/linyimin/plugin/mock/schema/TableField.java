package io.github.linyimin.plugin.mock.schema;

import com.alibaba.fastjson.annotation.JSONField;
import io.github.linyimin.plugin.mock.enums.FieldTypeEnum;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author banzhe
 * @date 2022/11/30 11:47
 **/
public class TableField {

    private static final Pattern FIELD_LENGTH_PATTERN = Pattern.compile("[a-zA-Z\\d\\s]+\\((\\d+)\\)");

    @JSONField(name = "Table")
    private String table;

    @JSONField(name = "Name")
    private String name;

    @JSONField(name = "Type")
    private String type;

    @JSONField(name = "Nullable")
    private boolean nullable;

    @JSONField(name ="Default")
    private String defaultValue;

    @JSONField(name = "Key")
    private String key;

    @JSONField(name = "Extra")
    private String extra;

    @JSONField(name = "Comment")
    private String comment;

    /**
     * {@link io.github.linyimin.plugin.mock.enums.MockTypeEnum}
     */
    @JSONField(name = "Mock Type")
    private String mockType;

    @JSONField(name = "Mock Value")
    private String mockParam;

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }


    public boolean isPrimaryKey() {
        if (StringUtils.isBlank(key)) {
            return false;
        }

        return StringUtils.contains(key, "PRI");
    }

    public boolean isAutoIncrement() {
        if (StringUtils.isBlank(extra)) {
            return false;
        }

        return StringUtils.contains(extra.toLowerCase(), "auto_increment");
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getActualType() {
        return parseType(type);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isNullable() {
        return nullable;
    }

    public void setNullable(boolean nullable) {
        this.nullable = nullable;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getMockType() {
        return mockType;
    }

    public void setMockType(String mockType) {
        this.mockType = mockType;
    }

    public String getMockParam() {
        return mockParam;
    }

    public void setMockParam(String mockParam) {
        this.mockParam = mockParam;
    }

    public int getFieldLength() {
        Matcher matcher = FIELD_LENGTH_PATTERN.matcher(this.type);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }

        return FieldTypeEnum.resolve(this.type).getLength();
    }

    public static boolean isNumber(String type) {

        List<FieldTypeEnum> numbers = Arrays.asList(
                FieldTypeEnum.TINYINT,
                FieldTypeEnum.SMALLINT,
                FieldTypeEnum.MEDIUMINT,
                FieldTypeEnum.INT,
                FieldTypeEnum.BIGINT,
                FieldTypeEnum.FLOAT,
                FieldTypeEnum.DOUBLE,
                FieldTypeEnum.DECIMAL
        );

        FieldTypeEnum fieldType = FieldTypeEnum.resolve(parseType(type));

        return numbers.contains(fieldType);

    }

    public static String parseType(String type) {
        Matcher matcher = FIELD_LENGTH_PATTERN.matcher(type);
        if (matcher.find()) {
            return type.substring(0, type.lastIndexOf("("));
        }

        return type;
    }
}
