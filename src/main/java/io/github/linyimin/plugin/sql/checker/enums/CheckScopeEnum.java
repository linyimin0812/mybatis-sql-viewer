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

    table,
    field,
    field_composition,

    mapper_xml,

    none
}
