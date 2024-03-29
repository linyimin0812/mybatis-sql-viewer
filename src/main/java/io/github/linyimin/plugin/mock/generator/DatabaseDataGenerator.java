package io.github.linyimin.plugin.mock.generator;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.intellij.openapi.project.Project;
import io.github.linyimin.plugin.mock.schema.TableField;
import io.github.linyimin.plugin.sql.executor.SqlExecutor;
import io.github.linyimin.plugin.sql.result.SelectResult;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

/**
 * @author banzhe
 * @date 2022/11/30 16:57
 **/
public class DatabaseDataGenerator implements DataGenerator<Object> {

    private Project project;

    private final LoadingCache<String, List<Object>> contentCache = CacheBuilder.newBuilder().expireAfterWrite(60, TimeUnit.MINUTES).build(new CacheLoader<String, List<Object>>() {
        @Override
        public List<Object> load(@NotNull String tableField) throws Exception {

            String table = tableField.split("\\.")[0];
            String fieldName = tableField.split("\\.")[1];

            String sql = String.format("SELECT %s FROM %s LIMIT 1000", fieldName, table);

            List<Object> contents = new ArrayList<>();

            SelectResult result = (SelectResult) SqlExecutor.executeSql(project, sql, false);

            Vector dataVector = result.getModel().getDataVector();

            for (Object data : dataVector) {
                Vector<Object> vector = (Vector<Object>) data;
                contents.add(vector.get(0));
            }

            return contents;
        }
    });

    @Override
    public Object generate(Project project, TableField field) throws Exception {

        this.project = project;

        String mockParam = field.getMockParam();
        if (StringUtils.isBlank(mockParam)) {
            return StringUtils.EMPTY;
        }

        List<Object> contents = contentCache.get(mockParam);

        if (CollectionUtils.isEmpty(contents)) {
            throw new RuntimeException(field.getMockParam() + " in table is empty.");
        }

        int index = RandomUtils.nextInt(0, contents.size());

        if (TableField.isNumber(field.getActualType())) {
            return Long.parseLong(contents.get(index).toString());
        }

        return contents.get(index).toString();
    }
}
