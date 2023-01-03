package io.github.linyimin.plugin.sql.checker.enums;

/**
 * @author banzhe
 * @date 2022/12/06 14:20
 **/
public enum CheckScopeEnum {
    insert,
    update,
    select,
    delete,

    naming_convention,

    field,
    field_composition,

    index_field,
    index_hit,

    none
}
