package io.github.linyimin.plugin.constant;

import com.google.common.collect.Lists;

import java.util.Arrays;
import java.util.List;

/**
 * @author yiminlin
 * @date 2022/01/31 2:32 上午
 * @description constant class
 **/
public class Constant {
    public static final String MAPPER = "mapper";
    public static final List<String> MYBATIS_OPS = Lists.newArrayList(
            "insert", "update", "delete", "select"
    );

    public static final String PARAM_ANNOTATION = "org.apache.ibatis.annotations.Param";

    public static final List<String> MYBATIS_SQL_ANNOTATIONS = Arrays.asList(
            "org.apache.ibatis.annotations.Insert",
            "org.apache.ibatis.annotations.Select",
            "org.apache.ibatis.annotations.Update",
            "org.apache.ibatis.annotations.Delete"
    );

    public static final String APPLICATION_NAME = "mybatis-sql-viewer";

    public static final String DATABASE_URL_TEMPLATE = "jdbc:mysql://%s:%s/%s";

    public static final int TABLE_ROW_HEIGHT = 20;

    public static final String TABLE_META_SQL_TEMPLATE = "SELECT `column_name` AS Name, \n" +
            "`column_type` AS Type, \n" +
            "CASE WHEN `IS_NULLABLE` = 'NO' THEN 'FALSE' ELSE 'TRUE' END AS Nullable,\n" +
            "`column_default` AS 'Default', \n" +
            "`column_key` AS 'key', \n" +
            "`EXTRA` AS Extra,\n" +
            "`column_comment` AS 'Comment' \n" +
            "FROM `information_schema`.`COLUMNS` \n" +
            "WHERE `table_name` = '${table}';";

    public static final String TABLE_INDEX_SQL_TEMPLATE = "SHOW INDEX FROM `${table}`;";

    public static int INSERT_ROWS = 1;

}
