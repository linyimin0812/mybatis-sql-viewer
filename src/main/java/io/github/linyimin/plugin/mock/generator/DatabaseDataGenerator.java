package io.github.linyimin.plugin.mock.generator;

import com.intellij.openapi.project.Project;
import io.github.linyimin.plugin.mock.schema.Field;
import io.github.linyimin.plugin.sql.executor.SqlExecutor;
import io.github.linyimin.plugin.sql.result.SelectResult;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * @author banzhe
 * @date 2022/11/30 16:57
 **/
public class DatabaseDataGenerator implements DataGenerator {

    @Override
    public List<Object> generate(Project project, Field field, int rows) throws Exception {

        List<Object> list = new ArrayList<>(rows);

        String mockParam = field.getMockParam();
        if (StringUtils.isBlank(mockParam)) {
            return list;
        }

        String table = mockParam.split("\\.")[0];
        String fieldName = mockParam.split("\\.")[1];

        String sql = String.format("SELECT %s FROM %s LIMIT 1000", fieldName, table);

        List<String> contents = new ArrayList<>();

        SelectResult result = (SelectResult) SqlExecutor.executeSql(project, sql, false);

        Vector<Vector<Object>> dataVector = result.getModel().getDataVector();

        for (Vector<Object> vector : dataVector) {
            contents.add(String.valueOf(vector.get(0)));
        }

        for (int i = 0; i < rows; i++) {
            int index = RandomUtils.nextInt(0, contents.size());
            list.add(contents.get(index));
        }

        return list;
    }
}
