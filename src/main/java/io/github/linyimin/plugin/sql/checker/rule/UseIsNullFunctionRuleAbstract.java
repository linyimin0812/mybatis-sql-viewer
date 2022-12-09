package io.github.linyimin.plugin.sql.checker.rule;

/**
 * @author banzhe
 * @date 2022/12/07 16:44
 **/
public abstract class UseIsNullFunctionRuleAbstract<T> extends SelectCheckRuleAbstract<T> {

    public final String DESC = "使用ISNULL()来判断是否为NULL值。因为NULL与任何值直接比较都为NULL。\n" +
            "   1）NULL <> NULL的返回结果是NULL，而不是false。\n" +
            "   2）NULL = NULL的返回结果是NULL，而不是true。\n" +
            "   3）NULL <> 1的返回结果是NULL，而不是true。";

    public final String SAMPLE = "在SQL语句中，如果在NULL前换行，影响可读性。SELECT * FROM table WHERE column1 IS NULL AND \n" +
            "column3 IS NOT NULL; 而ISNULL(column)是一个整体，简洁易懂。从性能数据上分析，ISNULL(column)的执行效率更快一些。";

    public UseIsNullFunctionRuleAbstract(Class<T> type) {
        super(type);
    }

}
