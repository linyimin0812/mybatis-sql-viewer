package io.github.linyimin.plugin.sql.checker;

import com.intellij.openapi.project.Project;
import io.github.linyimin.plugin.sql.checker.enums.CheckScopeEnum;
import io.github.linyimin.plugin.sql.checker.rule.CheckRule;

import java.util.ArrayList;
import java.util.List;

/**
 * @author banzhe
 * @date 2022/12/05 20:54
 **/
public abstract class Checker {

    abstract CheckScopeEnum scope();

    private final List<CheckRule> rules = new ArrayList<>();

    public void registerRule(CheckRule rule) {
        this.rules.add(rule);
    }

    public List<Report> check(Project project, String target) {
        List<Report> reports = new ArrayList<>();

        for (CheckRule rule : rules) {
            Report report = rule.check(project, target);
            if (report != null) {
                reports.add(report);
            }
        }

        return reports;
    }

}
