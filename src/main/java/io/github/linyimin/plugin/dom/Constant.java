package io.github.linyimin.plugin.dom;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.intellij.util.containers.ContainerUtil;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.*;
import java.util.Date;
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

    public static final String APPLICATION_NAME = "mybatis-sql-viewer";

    public static final String DATABASE_URL_TEMPLATE = "jdbc:mysql://%s:%s/%s";

    private static final String PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final DateFormat DF = new SimpleDateFormat(PATTERN);
    public static Map<String, Object> normalTypes = new ContainerUtil.ImmutableMapBuilder<String, Object>()
            .put("byte", 0)
            .put("java.lang.Byte", 0)
            .put("short", 0)
            .put("java.lang.Short", 0)
            .put("int", 0)
            .put("java.lang.Integer", 0)
            .put("long", 0)
            .put("java.lang.Long", 0)
            .put("float", 0.0)
            .put("java.lang.Float", 0.0)
            .put("double", 0.0)
            .put("java.lang.Double", 0.0)
            .put("boolean", true)
            .put("java.lang.Boolean", Boolean.TRUE)
            .put("java.math.BigDecimal", new BigDecimal(0))
            .put("char", "a")
            .put("java.lang.Character", "a")
            .put("java.lang.String", "")
            .put("java.util.Date", DF.format(new Date()))
            .put("java.sql.Date", DF.format(new Date()))
            .put("java.sql.Timestamp", System.currentTimeMillis())
            .put("java.time.LocalDate", LocalDate.now(ZoneId.of(JSON.defaultTimeZone.getID())).toString())
            .put("java.time.LocalTime", LocalTime.now(ZoneId.of(JSON.defaultTimeZone.getID())).toString())
            .put("java.time.LocalDateTime", LocalDateTime.now(ZoneId.of(JSON.defaultTimeZone.getID())).toString())
            .put("java.time.YearMonth", YearMonth.now(ZoneId.of(JSON.defaultTimeZone.getID())).toString())
            .build();

}
