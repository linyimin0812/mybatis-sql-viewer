package io.github.linyimin.plugin.mock.generator;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.intellij.openapi.project.Project;
import io.github.linyimin.plugin.mock.schema.Field;
import io.github.linyimin.plugin.sql.executor.SqlExecutor;
import io.github.linyimin.plugin.sql.result.SelectResult;
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
public class DatabaseDataGenerator implements DataGenerator {

    private Project project;

    private final LoadingCache<String, List<Object>> contentCache = CacheBuilder.newBuilder().expireAfterWrite(60, TimeUnit.MINUTES).build(new CacheLoader<String, List<Object>>() {
        @Override
        public List<Object> load(@NotNull String tableField) throws Exception {

            String table = tableField.split("\\.")[0];
            String fieldName = tableField.split("\\.")[1];

            String sql = String.format("SELECT %s FROM %s LIMIT 1000", fieldName, table);

            List<Object> contents = new ArrayList<>();

            SelectResult result = (SelectResult) SqlExecutor.executeSql(project, sql, false);

            Vector<Vector<Object>> dataVector = result.getModel().getDataVector();

            for (Vector<Object> vector : dataVector) {
                contents.add(vector.get(0));
            }

            return contents;
        }
    });

    @Override
    public Object generate(Project project, Field field) throws Exception {

        this.project = project;

        String mockParam = field.getMockParam();
        if (StringUtils.isBlank(mockParam)) {
            return StringUtils.EMPTY;
        }

        List<Object> contents = contentCache.get(mockParam);

        int index = RandomUtils.nextInt(0, contents.size());
        return contents.get(index);
    }
}
