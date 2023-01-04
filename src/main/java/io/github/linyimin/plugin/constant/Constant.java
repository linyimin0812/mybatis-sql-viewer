package io.github.linyimin.plugin.constant;

import com.google.common.collect.Lists;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.UIUtil;
import io.github.linyimin.plugin.sql.checker.Report;
import io.github.linyimin.plugin.sql.checker.enums.CheckScopeEnum;
import io.github.linyimin.plugin.sql.checker.enums.LevelEnum;

import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            "`column_key` AS 'Key', \n" +
            "`EXTRA` AS Extra,\n" +
            "`column_comment` AS 'Comment' \n" +
            "FROM `information_schema`.`COLUMNS` \n" +
            "WHERE `table_name` = '${table}';";

    public static final String TABLE_INDEX_SQL_TEMPLATE = "SHOW INDEX FROM `${table}`;";

    public static int INSERT_ROWS = 10000;

    public static String SQL_STATEMENT_LOADING_PROMPT = "Loading SQL Statement...";

    public static final String SOURCE_CODE = "https://github.com/linyimin-bupt/mybatis-sql-viewer";


    public static final List<Report> DEFAULT_INDEX_REPORTS = Arrays.asList(
            new Report().isPass(false).level(LevelEnum.for_reference).desc("业务上具有唯一特性的字段，即使是组合字段，也必须建成唯一索引。\n" +
                    "  不要以为唯一索引影响了insert速度，这个速度损耗可以忽略，但是提高查找速度是明显的；另外，即使应用层做了非常完善的校验机制，只要没有位移索引，根据墨菲定律，必然有脏数据产生。"),
            new Report().isPass(false).level(LevelEnum.for_reference).desc("创建索引时避免如下极端误解：\n" +
                    "  1）索引宁缺毋滥、认为一个查询就需要建一个索引；\n" +
                    "  2）吝啬索引的创建。认为索引会消耗空间、严重拖慢记录的更新以及行的新增速度；\n" +
                    "  3）抵制唯一索引。认为唯一索引一律需要在应用层通过\"先查后插\"方式解决。"),
            new Report().isPass(false).level(LevelEnum.for_reference).desc("不得使用外键与级联，一切外键概念必须在应用层解决。\n" +
                    "  (概念解释)学生表中的student_id是主键，那么成绩表中的student_id则为外键。如果更新学生表中的student_id，同时触发成绩表中的student_id更新，即为级联\n" +
                    "  更新。外键与级联更新使用于单机低并发，不适合分布式、高并发集群；级联更新是强阻塞，存在数据库更新风暴的风险；外键影响数据库的插入速度。")
    );

    public static final List<Report> DEFAULT_TABLE_REPORTS = Arrays.asList(
            new Report().isPass(false).level(LevelEnum.for_reference).desc("如果存储的字符串长度几乎相等，使用char定长字符串类型。"),
            new Report().isPass(false).level(LevelEnum.for_reference).desc("表的命名最好是遵循\"业务名称_表的作用\"。\n" +
                    "  如：alipay_task/force_project/trade_config"),
            new Report().isPass(false).level(LevelEnum.for_reference).desc("库名与应用名尽量一致。"),
            new Report().isPass(false).level(LevelEnum.for_reference).desc("如果修改字段含义或对字段表示的状态追加时，需要及时更新字段注释。"),
            new Report().isPass(false).level(LevelEnum.for_reference).desc("字段允许适当冗余，以提高查询性能，但必须考虑数据一致性。冗余字段应遵循：\n" +
                    "  1）不是频繁修改的字段；\n" +
                    "  2）不是唯一索引的字段；\n" +
                    "  3）不是varchar超长字段，更不能是text字段；\n" +
                    "  如：更业务线经常冗余存储商品名称，避免查询时需要调用IC服务获取。"),
            new Report().isPass(false).level(LevelEnum.for_reference).desc("单表行数超过500万行或者单表容量超过2GB，才推荐进行分库分表。\n" +
                    "  如果预计三年后的数据量根本达不到这个级别，请不要在创建表时就分库分表。"),
            new Report().isPass(false).level(LevelEnum.for_reference).desc("合适的字符存储长度，不但节约数据库表空间、节约索引存储，更重要的是提升检索速度。\n" +
                    "  无符号值可以避免误存负数，且扩大了表示范围。")

    );

    public static final List<Report> DEFAULT_SELECT_REPORTS = Arrays.asList(
            new Report().isPass(false).level(LevelEnum.for_reference).desc("防止因字段类型不同造成的隐式转换，导致索引失效"),
            new Report().isPass(false).level(LevelEnum.for_reference).desc("数据订正（特别是删除或修改记录操作）时，要先select，避免出现误删除，确认无误后才能执行更新语句。"),
            new Report().isPass(false).level(LevelEnum.for_reference).desc("禁止使用存储过程，存储过程难以调试和扩展，更没有移植性。")
    );

    public static final Map<CheckScopeEnum, List<Report>> DEFAULT_REPORT_MAP = new HashMap<CheckScopeEnum, List<Report>>() {{
        put(CheckScopeEnum.index_field, DEFAULT_INDEX_REPORTS);
        put(CheckScopeEnum.naming_convention, DEFAULT_TABLE_REPORTS);
        put(CheckScopeEnum.select, DEFAULT_SELECT_REPORTS);
        put(CheckScopeEnum.update, DEFAULT_SELECT_REPORTS);
        put(CheckScopeEnum.delete, DEFAULT_SELECT_REPORTS);
    }};

    public static final Border LINE_BORDER = UIUtil.isUnderDarcula() ? new LineBorder(JBColor.BLACK) : new LineBorder(JBColor.lightGray);

    public static final String INPUT_SQL_PROMPT = "Please input sql statement.";

    public static final String ROOT_NAME = "Mybatis Sql";

    public static final String DATASOURCE_CONNECTED = "Server Connected.";

}
