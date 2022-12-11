package io.github.linyimin.plugin.sql.builder;

import com.intellij.openapi.project.Project;
import io.github.linyimin.plugin.mock.enums.MockTypeEnum;
import io.github.linyimin.plugin.mock.generator.DataGenerator;
import io.github.linyimin.plugin.mock.generator.DataGeneratorFactory;
import io.github.linyimin.plugin.mock.schema.Field;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author banzhe
 * @date 2022/12/02 14:36
 **/
public class SqlBuilder {

    private static final String INSERT_TEMPLATE = "INSERT INTO %s (%s) values %s";

    public static String buildInsertSql(Project project, String table, List<Field> fields) throws Exception {

        List<Object> mockData = mockData(project, fields);

        String columns = fields.stream().map(Field::getName).collect(Collectors.joining(", "));

        String values = encapsulateValue(mockData);

        return String.format(INSERT_TEMPLATE, table, columns, values);

    }


    public static String buildInsertSqlBatch(Project project, String table, List<Field> fields, int rows) throws Exception {

        String columns = fields.stream().map(Field::getName).collect(Collectors.joining(", "));

        List<List<Object>> subMockData = new ArrayList<>();
        for (int i = 0; i < rows; i++) {
            subMockData.add(mockData(project, fields));
        }
        List<String> values = new ArrayList<>();
        for (List<Object> rowValues : subMockData) {
            values.add(encapsulateValue(rowValues));
        }

        return String.format(INSERT_TEMPLATE, table, columns, String.join(", ", values));
    }

    private static List<String> encapsulateValueSingle(String table, List<List<Object>> mockData, List<Field> fields, int rows) {

        String columns = fields.stream().map(Field::getName).collect(Collectors.joining(", "));

        List<String> sqls = new ArrayList<>();

        for (int i = 0; i < rows; i++) {
            // 批量插入, 默认每次插入100条
            String values = encapsulateValue(mockData.get(i));
            sqls.add(String.format(INSERT_TEMPLATE, table, columns, values));
        }

        return sqls;
    }

    private static String encapsulateValue(List<Object> rowValues) {

        StringBuilder values = new StringBuilder().append("(");
        for (int index = 0; index < rowValues.size(); index++) {
            Object value = rowValues.get(index);
            if (value instanceof Number) {
                values.append(value);
            } else {
                value = ((String) value).replaceAll("'", "\\\\'");
                values.append("'").append(value).append("'");
            }
            if (index + 1 != rowValues.size()) {
                values.append(", ");
            }
        }
        values.append(")");

        return values.toString();
    }

    public static List<Object> mockData(Project project, List<Field> fields) throws Exception {

        List<Object> mockData = new ArrayList<>(fields.size());


        for (Field field : fields) {

            MockTypeEnum type = MockTypeEnum.valueOf(field.getMockType());
            DataGenerator generator = DataGeneratorFactory.getGenerator(type);
            Object fieldData = generator.generate(project, field);

            mockData.add(fieldData);
        }

        return mockData;
    }

}
