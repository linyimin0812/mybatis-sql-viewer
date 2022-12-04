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

    public static List<String> buildInsertSql(Project project, String table, List<Field> fields, int rows, boolean batch) throws Exception {

        List<List<Object>> mockData = mockData(project, fields, rows);

        if (!batch) {
            return encapsulateValueSingle(table, mockData, fields, rows);
        }

        return encapsulateValueBatch(table, mockData, fields, rows);
    }

    private static List<String> encapsulateValueBatch(String table, List<List<Object>> mockData, List<Field> fields, int rows) {

        String columns = fields.stream().map(Field::getName).collect(Collectors.joining(", "));

        List<String> sqls = new ArrayList<>();

        int batch = 100;
        int index = 0;
        while (index < rows) {
            int end = Math.min(index + batch, rows);
            List<List<Object>> subMockData = mockData.subList(index, end);
            List<String> values = new ArrayList<>();
            for (List<Object> rowValues : subMockData) {
                values.add(encapsulateValue(rowValues));
            }
            sqls.add(String.format(INSERT_TEMPLATE, table, columns, String.join(", ", values)));

            index = end;
        }

        return sqls;
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

    public static List<List<Object>> mockData(Project project, List<Field> fields, int rows) throws Exception {

        List<List<Object>> mockData = new ArrayList<>(rows);

        for (int i = 0; i < rows; i++) {
            mockData.add(new ArrayList<>());
        }

        for (Field field : fields) {

            MockTypeEnum type = MockTypeEnum.valueOf(field.getMockType());
            DataGenerator generator = DataGeneratorFactory.getGenerator(type);
            List<String> fieldData = generator.generate(project, field, rows);

            for (int j = 0; j < rows; j++) {
                mockData.get(j).add(fieldData.get(j));
            }
        }

        return mockData;
    }

}
