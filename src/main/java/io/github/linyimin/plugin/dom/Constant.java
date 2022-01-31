package io.github.linyimin.plugin.dom;

import com.google.common.collect.Lists;

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
}
