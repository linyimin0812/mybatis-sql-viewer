package io.github.linyimin.plugin.sql.checker;

import io.github.linyimin.plugin.sql.checker.enums.CheckScopeEnum;
import io.github.linyimin.plugin.sql.checker.rule.CheckRule;

import java.util.*;

/**
 * @author banzhe
 * @date 2022/12/06 14:03
 **/
public class CheckerHolder {

    private static final Map<CheckScopeEnum, Checker> checkers;

    static {
        checkers = new HashMap<>();
        ServiceLoader<Checker> checkerLoader = ServiceLoader.load(Checker.class, CheckerHolder.class.getClassLoader());
        for (Checker checker : checkerLoader) {
            CheckerHolder.registerChecker(checker);
        }

        ServiceLoader<CheckRule> checkRuleLoader = ServiceLoader.load(CheckRule.class, CheckerHolder.class.getClassLoader());
        for (CheckRule rule : checkRuleLoader) {
            List<CheckScopeEnum> scopes = rule.scopes();
            for (CheckScopeEnum scope : scopes) {
                if (!CheckerHolder.checkers.containsKey(scope)) {
                    continue;
                }

                CheckerHolder.checkers.get(scope).registerRule(rule);
            }
        }
    }

    public static void registerChecker(Checker checker) {


        checkers.put(checker.scope(), checker);
    }

    public static void unRegisterChecker(Checker checker) {
        checkers.remove(checker.scope());
    }

    public static Collection<Checker> getCheckers() {
        return checkers.values();
    }

    public static Checker getChecker(CheckScopeEnum scope) {
        return checkers.get(scope);
    }

    public static List<Checker> getCheckers(CheckScopeEnum... scopes) {

        List<Checker> checkerList = new ArrayList<>();

        for (CheckScopeEnum scope : scopes) {
            if (checkers.containsKey(scope)) {
                checkerList.add(checkers.get(scope));
            }
        }

        return checkerList;
    }

}
