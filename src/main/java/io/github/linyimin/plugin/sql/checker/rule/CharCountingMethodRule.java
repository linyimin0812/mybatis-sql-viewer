package io.github.linyimin.plugin.sql.checker.rule;

import io.github.linyimin.plugin.sql.checker.Report;
import io.github.linyimin.plugin.sql.checker.enums.LevelEnum;
import net.sf.jsqlparser.expression.Function;
import org.apache.commons.lang3.StringUtils;


/**
 * @author banzhe
 * @date 2022/12/06 21:32
 * 字符计数方法
 **/
public class CharCountingMethodRule extends SelectCheckRuleAbstract<Function> {

    public CharCountingMethodRule() {
        super(Function.class);
    }

    @Override
    Report doCheck(Function function) {

        Report report = new Report().isPass(true);

        String name = function.getName();

        if (StringUtils.equalsAnyIgnoreCase(name,"CHARACTER_LENGTH", "LENGTH")) {

            String desc = "因国际化需要，所有的字符存储与表示，均采用utf8字符集，那么字符统计方法需要注意。\n" +
                    "   1）SELECT LENGTH(\"轻松工作\"); 返回为12\n" +
                    "   2）SELECT CHARACTER_LENGTH(\"轻松工作\"); 返回为4";

            return report.isPass(false).desc(desc).level(LevelEnum.for_reference);
        }

        return report;
    }

}
