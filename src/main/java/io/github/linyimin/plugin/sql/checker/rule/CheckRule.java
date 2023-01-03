package io.github.linyimin.plugin.sql.checker.rule;

import com.intellij.openapi.project.Project;
import io.github.linyimin.plugin.sql.checker.enums.CheckScopeEnum;
import io.github.linyimin.plugin.sql.checker.Report;

import java.util.List;

/**
 * @author banzhe
 * @date 2022/12/06 14:09
 **/
public interface CheckRule {
    /**
     * @param target 需要检查的对象(sql, table, xml)
     * @return 规则检查报告
     */
    Report check(Project project, String target);

    List<CheckScopeEnum> scopes();

}
