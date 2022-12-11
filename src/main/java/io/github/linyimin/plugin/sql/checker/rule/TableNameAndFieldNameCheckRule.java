package io.github.linyimin.plugin.sql.checker.rule;

import io.github.linyimin.plugin.sql.checker.Report;
import io.github.linyimin.plugin.sql.checker.enums.CheckScopeEnum;
import io.github.linyimin.plugin.sql.checker.enums.LevelEnum;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author banzhe
 * @date 2022/12/09 19:58
 **/
public class TableNameAndFieldNameCheckRule implements CheckRule {

    private final Pattern UPPERCASE = Pattern.compile("[A-Z]+");
    private final Pattern START_WITH_DIGIT = Pattern.compile("^\\d");
    private final Pattern ONLY_DIGIT_IN_THE_MIDDLE_OF_UNDERSCORE = Pattern.compile("_\\d+_");

    @Override
    public Report check(String target) {

       if (UPPERCASE.matcher(target).find() || START_WITH_DIGIT.matcher(target).find() || ONLY_DIGIT_IN_THE_MIDDLE_OF_UNDERSCORE.matcher(target).find()) {
           String desc = "表名、字段名必须使用小写字母或数字，禁止出现数字开头，禁止两个下划线中间只出现数字。" +
                   "    数据库字段名的修改代价很大，因为无法进行预发布，所以字段名称需要慎重考虑。" +
                   "    MySQL在Windows下不区分大小写，但在Linux下默认是区分大小写。因此，数据库名、表名、字段名，都不允许出现任何大写字母，避免节外生枝。";
           String sample = "正例：aliyun_admin, rdc_config, level3_name\n" +
                   "  反例：AliyunAdmin, rdcConfig, level_3_name";
           return new Report().isPass(false).level(LevelEnum.mandatory).desc(desc).sample(sample);
       }

        return new Report().isPass(true);
    }

    @Override
    public List<CheckScopeEnum> scopes() {
        return Arrays.asList(CheckScopeEnum.table, CheckScopeEnum.field);
    }
}
