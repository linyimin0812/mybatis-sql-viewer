package io.github.linyimin.plugin.mock.enums;

/**
 * @author banzhe
 * @date 2022/11/29 21:28
 **/
public enum MockTypeEnum {
    increment,
    fixed,
    random,
    regex,
    lexicon,
    database,
    /**
     * 不模拟，直接使用默认值
     */
    none
}
