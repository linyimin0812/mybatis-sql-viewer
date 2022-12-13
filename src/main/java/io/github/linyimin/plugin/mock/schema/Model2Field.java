package io.github.linyimin.plugin.mock.schema;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.GsonBuilder;
import org.apache.commons.lang3.StringUtils;

import javax.swing.table.DefaultTableModel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * @author banzhe
 * @date 2022/12/13 23:45
 **/
public class Model2Field {

    public static <T> List<T> parse(Class<T> clazz, DefaultTableModel model) {
        Vector<Map<String, String>> configs = new Vector<>();
        Vector dataVector = model.getDataVector();
        for (Object values : dataVector) {
            Map<String, String> config = new HashMap<>();
            Vector vector = (Vector) values;
            for (int i = 0; i < model.getColumnCount(); i++) {
                String value =  vector.get(i) == null ? StringUtils.EMPTY : String.valueOf(vector.get(i));
                config.put(model.getColumnName(i), value);
            }
            configs.add(config);
        }
        String config = new GsonBuilder().setPrettyPrinting().create().toJson(configs);

        return JSONObject.parseArray(config, clazz);
    }
}
