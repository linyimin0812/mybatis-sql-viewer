package io.github.linyimin.plugin.mybatis.scripting.tags;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import com.alibaba.fastjson.parser.Feature;
import io.github.linyimin.plugin.mybatis.type.SimpleTypeRegistry;
import ognl.OgnlContext;
import ognl.OgnlRuntime;
import ognl.PropertyAccessor;

import java.util.*;

/**
 * @author Clinton Begin
 */
public class DynamicContext {

    public static final String PARAMETER_OBJECT_KEY = "_parameter";

    static {
        OgnlRuntime.setPropertyAccessor(ContextMap.class, new ContextAccessor());
    }

    private final ContextMap bindings;
    private final StringJoiner sqlBuilder = new StringJoiner(" ");
    private int uniqueNumber = 0;

    public DynamicContext(Object parameterObject) {

        bindings = new ContextMap(parameterObject);

        bindings.put(PARAMETER_OBJECT_KEY, parameterObject);

        if (parameterObject instanceof Collection) {
            bindings.put("collection", parameterObject);

            if (parameterObject instanceof List) {
                bindings.put("list", parameterObject);
            }
            return;
        }
        if (parameterObject != null && parameterObject.getClass().isArray()) {
            bindings.put("array", parameterObject);
            return;
        }

        if (parameterObject != null && JSONObject.isValidArray(parameterObject.toString())) {
            JSONArray array = JSONObject.parseArray(parameterObject.toString(), Feature.DisableCircularReferenceDetect);
            bindings.put("collection", array);
            bindings.put("list", array);
            bindings.put("array", array);
        }

    }

    public Map<String, Object> getBindings() {
        return bindings;
    }

    public void bind(String name, Object value) {

        bindings.put(name, value);
    }

    public void appendSql(String sql) {
        sqlBuilder.add(sql);
    }

    public String getSql() {
        return sqlBuilder.toString().trim();
    }

    public int getUniqueNumber() {
        return uniqueNumber++;
    }

    static class ContextMap extends HashMap<String, Object> {

        private static final long serialVersionUID = 2977601501966151582L;
        private final JSONObject parameterJSONObject;
        private final Object parameterObject;

        public ContextMap(Object parameterObject) {

            this.parameterObject = parameterObject;

            if (parameterObject == null) {
                this.parameterJSONObject = null;
                return;
            }

            if (parameterObject.getClass() == Date.class) {
                parameterObject = parameterObject.toString();
            }

            String parameterObjectStr = parameterObject.toString();

            if (!SimpleTypeRegistry.isSimpleType(parameterObject.getClass())) {
                parameterObjectStr = JSONObject.toJSONString(parameterObject);
            }

            if (JSONObject.isValidObject(parameterObjectStr)) {
                this.parameterJSONObject = JSONObject.parseObject(parameterObjectStr);
            } else {
                this.parameterJSONObject = null;
            }
        }

        @Override
        public Object get(Object key) {

            String strKey = (String) key;

            if (super.containsKey(strKey)) {
                return super.get(strKey);
            }

            if (strKey.contains(ForEachSqlNode.ITEM_PREFIX)) {
                return JSONPath.eval(this, strKey);
            }

            if (parameterJSONObject != null) {
                return JSONPath.eval(parameterJSONObject, strKey);
            }

            if (parameterObject == null) {
                return null;
            }

            if (SimpleTypeRegistry.isSimpleType(parameterObject.getClass())) {
                return parameterObject;
            }

            return null;
        }
    }

    static class ContextAccessor implements PropertyAccessor {

        @Override
        public Object getProperty(Map context, Object target, Object name) {
            Map map = (Map) target;

            Object result = map.get(name);
            if (map.containsKey(name) || result != null) {
                return result;
            }

            Object parameterObject = map.get(PARAMETER_OBJECT_KEY);
            if (parameterObject instanceof Map) {
                return ((Map)parameterObject).get(name);
            }

            return null;
        }

        @Override
        public void setProperty(Map context, Object target, Object name, Object value) {
            Map<Object, Object> map = (Map<Object, Object>) target;
            map.put(name, value);
        }

        @Override
        public String getSourceAccessor(OgnlContext arg0, Object arg1, Object arg2) {
            return null;
        }

        @Override
        public String getSourceSetter(OgnlContext arg0, Object arg1, Object arg2) {
            return null;
        }
    }
}
