package io.github.linyimin.plugin.sql.converter;

import io.github.linyimin.plugin.sql.result.BaseResult;
import io.github.linyimin.plugin.sql.result.SelectResult;
import io.github.linyimin.plugin.sql.result.UpdateResult;
import org.apache.commons.lang3.StringUtils;

import javax.swing.table.DefaultTableModel;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Vector;

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
            if (++count > 100) {
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
            sb.append("[Rows Affected]: ").append(((UpdateResult) result).getAffectedCount());
        } else {
            sb.append("[Rows Return]: ").append(((SelectResult)result).getModel().getRowCount());
        }

        return sb.toString();
    }
}
