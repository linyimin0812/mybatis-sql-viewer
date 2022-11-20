package io.github.linyimin.plugin.mybatis.scripting.tags;

/**
 * @author Clinton Begin
 */
public interface SqlNode {
    boolean apply(DynamicContext context);
}

