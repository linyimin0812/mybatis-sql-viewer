package io.github.linyimin.plugin.mybatis.scripting.tags;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import com.alibaba.fastjson.parser.Feature;
import io.github.linyimin.plugin.component.SqlParamGenerateComponent;
import io.github.linyimin.plugin.mybatis.type.SimpleTypeRegistry;
import ognl.OgnlContext;
import ognl.OgnlRuntime;
import ognl.PropertyAccessor;
import org.apache.commons.collections.CollectionUtils;

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

    public DynamicContext(List<SqlParamGenerateComponent.ParamNameType> types, Object parameterObject) {

        bindings = new ContextMap(types, parameterObject);

        if (parameterObject != null) {
            JSONObject object = JSONObject.parseObject(parameterObject.toString());
            if (object.size() != 1) {
                return;
            }
            Collection<Object> values = object.values();
            for (Object value : values) {
                if (JSONObject.isValidArray(value.toString())) {
                    JSONArray array = JSONObject.parseArray(value.toString(), Feature.DisableCircularReferenceDetect);
                    bindings.put("collection", array);
                    bindings.put("list", array);
                    bindings.put("array", array);
                }
            }
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
        private JSONObject parameterJSONObject;
        private List<SqlParamGenerateComponent.ParamNameType> types;
        private boolean hasReturned = false;

        public ContextMap(List<SqlParamGenerateComponent.ParamNameType> types, Object parameterObject) {

            if (parameterObject == null) {
                return;
            }

            this.types = types;
            this.parameterJSONObject = JSONObject.parseObject(parameterObject.toString(), Feature.DisableCircularReferenceDetect);

            if (CollectionUtils.isNotEmpty(this.types) && this.types.size() == 1 && this.parameterJSONObject.values().size() == 1) {
                String clazz = this.types.get(0).getPsiType().getCanonicalText();
                if (SimpleTypeRegistry.isSimpleType(clazz)) {
                    Object value = this.parameterJSONObject.values().stream().findAny().orElse(null);
                    super.put(PARAMETER_OBJECT_KEY, value);
                }
            }
        }

        @Override
        public Object get(Object key) {

            String strKey = (String) key;

            Object value = null;

            if (super.containsKey(strKey)) {
                value = super.get(strKey);
            } else if (strKey.contains(ForEachSqlNode.ITEM_PREFIX)) {
                value = JSONPath.eval(this, strKey);
            } else if (parameterJSONObject != null && JSONPath.contains(parameterJSONObject, strKey)) {
                value = JSONPath.eval(parameterJSONObject, strKey);
            }else if (CollectionUtils.isNotEmpty(this.types) && this.types.size() == 1 && !hasReturned) {
                String clazz = this.types.get(0).getPsiType().getCanonicalText();
                if (SimpleTypeRegistry.isSimpleType(clazz)) {
                    value = super.get(PARAMETER_OBJECT_KEY);
                }
            }
            hasReturned = true;
            return value;
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
