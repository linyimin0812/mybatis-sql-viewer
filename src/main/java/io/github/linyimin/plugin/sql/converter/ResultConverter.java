package io.github.linyimin.plugin.sql.converter;

import io.github.linyimin.plugin.constant.Constant;
import io.github.linyimin.plugin.sql.checker.Report;
import io.github.linyimin.plugin.sql.checker.enums.CheckScopeEnum;
import io.github.linyimin.plugin.sql.result.BaseResult;
import io.github.linyimin.plugin.sql.result.InsertResult;
import io.github.linyimin.plugin.sql.result.SelectResult;
import io.github.linyimin.plugin.sql.result.UpdateResult;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import javax.swing.table.DefaultTableModel;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

/**
 * @author yiminlin
 * @date 2022/11/27 20:45
 **/
public class ResultConverter {

    public static DefaultTableModel convert2TableModel(ResultSet rs) throws SQLException {

        ResultSetMetaData metaData = rs.getMetaData();

        // names of columns
        Vector<String> columnNames = new Vector<>();
        int columnCount = metaData.getColumnCount();
        for (int column = 1; column <= columnCount; column++) {
            columnNames.add(metaData.getColumnLabel(column));
        }

        // data of the table
        Vector<Vector<Object>> data = new Vector<>();
        int count = 0;
        while (rs.next()) {
            Vector<Object> vector = new Vector<>();
            for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
                vector.add(rs.getObject(columnIndex));
            }
            data.add(vector);
            // TODO: 配置项？最大返回记录数
            if (++count >= 100) {
                break;
            }
        }

        rs.close();

        return new DefaultTableModel(data, columnNames);
    }

    public static String convert2ExecuteInfo(BaseResult result) {
        StringBuilder sb = new StringBuilder("------[Execution Succeeded]------\n");

        if (StringUtils.isNotBlank(result.getSql())) {
            sb.append("[statement]: ").append(result.getSql()).append("\n");
        }

        sb.append("[cost]: ").append(result.getCost()).append("(ms)").append("\n");
        if (result instanceof UpdateResult) {
            sb.append("[Rows Affected]: ").append(((UpdateResult) result).getAffectedCount()).append("\n");
        } else {
            sb.append("[Return Rows]: ").append(((SelectResult)result).getModel().getRowCount()).append("\n");
        }

        if (result.getTotalRows().size() == 1) {
            sb.append("[Total Rows]: ").append(result.getTotalRows().get(0).getValue()).append("\n");
        } else {
            for (Pair<String, Long> pair : result.getTotalRows()) {
                String table = pair.getKey().replaceAll("`", "");
                sb.append("[Total Rows(").append(table).append(")]: ").append(pair.getValue()).append("\n");
            }
        }

        return sb.toString();
    }

    public static String convert2InsertInfo(InsertResult result) {

        return "------[Insertion Succeeded]------\n"
                + "[Cost]: " + result.getCost() + "(ms)\n"
                + "[Rows Affected]: " + result.getAffectedCount() + "\n"
                + "[Total Rows]: " + result.getTotalRows().get(0).getValue() + "\n";
    }

    public static String convert2RuleInfo(CheckScopeEnum scope, List<Report> reports) {

        List<Report> noPassReports = reports.stream().filter(report -> !report.isPass()).collect(Collectors.toList());

        StringBuilder sb = new StringBuilder();

        if (CollectionUtils.isEmpty(noPassReports)) {
            sb.append("符合SQL规范要求\n").append("\n");
            noPassReports.addAll(Constant.DEFAULT_REPORT_MAP.getOrDefault(scope, new ArrayList<>()));
        } else {
            sb.append("不符合SQL规范要求, 不满足以下规范：\n").append("\n");
        }

        for (int i = 0; i < noPassReports.size(); i++) {
            Report report = noPassReports.get(i);
            sb.append(i + 1).append(".【").append(report.getLevel().name()).append("】");
            sb.append(report.getDesc()).append("\n");
            if (StringUtils.isNotBlank(report.getSample())) {
                sb.append("  【sample】\n").append("    ").append(report.getSample()).append("\n");
            }
            sb.append("\n");
        }

        return sb.toString();
    }
}
