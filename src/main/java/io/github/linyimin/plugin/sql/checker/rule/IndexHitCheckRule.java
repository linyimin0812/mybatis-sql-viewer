package io.github.linyimin.plugin.sql.checker.rule;

import com.intellij.openapi.project.Project;
import io.github.linyimin.plugin.sql.checker.Report;
import io.github.linyimin.plugin.sql.checker.enums.CheckScopeEnum;
import io.github.linyimin.plugin.sql.checker.enums.LevelEnum;
import io.github.linyimin.plugin.sql.executor.SqlExecutor;
import io.github.linyimin.plugin.sql.parser.SqlParser;
import io.github.linyimin.plugin.sql.result.SelectResult;
import org.apache.commons.lang3.StringUtils;

import javax.swing.table.DefaultTableModel;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author banzhe
 **/
public class IndexHitCheckRule implements CheckRule {
    @Override
    public Report check(Project project, String target) {

        CheckScopeEnum scope = SqlParser.getCheckScope(target);
        if (scope == CheckScopeEnum.insert) {
            return new Report().isPass(true);
        }

        String explainSql = String.format("EXPLAIN %s", target);
        try {
            SelectResult result = (SelectResult) SqlExecutor.executeSql(project, explainSql, false);
            DefaultTableModel model = result.getModel();
            if (model.getRowCount() == 0) {
                return new Report().isPass(false).level(LevelEnum.error).desc("EXPLAIN statement result is emtpy.");
            }

            int typeIndex = model.findColumn("type");

            List<IndexType> indexTypes = new ArrayList<>();

            for (int row = 0; row < model.getRowCount(); row++) {
                String type = (String) model.getValueAt(row, typeIndex);
                IndexType indexType = IndexType.resolveByValue(type);
                indexTypes.add(indexType);
            }

            boolean isScanAll = indexTypes.stream().anyMatch(indexType -> indexType.priority == IndexType.all.priority);

            if (isScanAll) {
                String desc = "存在全表扫描，请优化相关语句。";
                return new Report().isPass(false).level(LevelEnum.mandatory).desc(desc);
            }

            boolean isIndexScanAll = indexTypes.stream().anyMatch(indexType -> indexType.priority <= IndexType.index.priority);
            if (isIndexScanAll) {
                String desc = "SQL性能优化的目标：至少要达到range级别，要求是ref级别，如果可以是consts最好。\n" +
                        "   1）consts单表中最多只有一个匹配行(主键或者唯一索引)，在优化阶段即可读取到数据。\n" +
                        "   2）ref指的是使用普通的索引。\n" +
                        "   3）range对索引进行范围检索。";
                String sample = "EXPLAIN表的结果，type=index，索引物理文件全扫描，速度非常慢，这个index级别比range还低，与全表扫描是小巫见大巫。";
                return new Report().isPass(false).level(LevelEnum.mandatory).desc(desc).sample(sample);
            }

        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            return new Report().isPass(false).level(LevelEnum.error).desc(sw.toString());
        }

        return new Report().isPass(true);
    }

    @Override
    public List<CheckScopeEnum> scopes() {
        return Collections.singletonList(CheckScopeEnum.index_hit);
    }

    private enum IndexType {


        all("all", 0),
        index("index", 1),
        range("range", 2),
        ref("ref", 3),
        eq_ref("eq_ref", 4),
        __const__("const", 5),
        system("system", 6),
        NULL(null, 7);

        private final String value;
        private final int priority;

        IndexType(String value, int priority) {
            this.value = value;
            this.priority = priority;
        }


        public static IndexType resolveByValue(String value) {
            return Arrays.stream(IndexType.values()).filter(type -> StringUtils.equalsIgnoreCase(type.value, value)).findFirst().orElse(IndexType.NULL);
        }
    }
}
