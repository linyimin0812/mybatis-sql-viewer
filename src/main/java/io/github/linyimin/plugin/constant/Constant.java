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

}
