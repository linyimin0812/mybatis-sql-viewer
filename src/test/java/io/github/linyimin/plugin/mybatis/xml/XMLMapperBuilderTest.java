package io.github.linyimin.plugin.mybatis.xml;

import io.github.linyimin.plugin.mybatis.mapping.SqlSource;
import io.github.linyimin.plugin.utils.LoadXmlUtil;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class XMLMapperBuilderTest {

    @Test
    public void testBasic() {

        String expectedSql = "SELECT\n" +
                "  name,\n" +
                "  category,\n" +
                "  price\n" +
                "FROM\n" +
                "  fruits\n" +
                "WHERE\n" +
                "  category = 'apple'\n" +
                "  AND price < 500";

        XMLMapperBuilder builder = new XMLMapperBuilder(LoadXmlUtil.load("basic-fruits.xml"));
        Map<String, SqlSource> sqlSourceMap = builder.parse();
        Assertions.assertNotNull(sqlSourceMap);
        Assertions.assertTrue(sqlSourceMap.containsKey("testBasic"));

        SqlSource sqlSource = sqlSourceMap.get("testBasic");

        Assertions.assertEquals(expectedSql, sqlSource.getSql(null));

    }

    @Test
    public void testBind() {

        String expectedSql = "SELECT\n" +
                "  name,\n" +
                "  category,\n" +
                "  price\n" +
                "FROM\n" +
                "  fruits\n" +
                "WHERE\n" +
                "  name LIKE '%testBind%'";

        XMLMapperBuilder builder = new XMLMapperBuilder(LoadXmlUtil.load("bind-fruits.xml"));
        Map<String, SqlSource> sqlSourceMap = builder.parse();
        Assertions.assertNotNull(sqlSourceMap);
        Assertions.assertTrue(sqlSourceMap.containsKey("testBind"));

        SqlSource sqlSource = sqlSourceMap.get("testBind");

        Assertions.assertEquals(expectedSql, sqlSource.getSql("testBind"));

        Assertions.assertEquals(expectedSql, sqlSource.getSql("{\"name\": \"testBind\"}"));

    }

    @Test
    public void testChoose() {

        XMLMapperBuilder builder = new XMLMapperBuilder(LoadXmlUtil.load("choose-fruits.xml"));
        Map<String, SqlSource> sqlSourceMap = builder.parse();
        Assertions.assertNotNull(sqlSourceMap);
        Assertions.assertTrue(sqlSourceMap.containsKey("testChoose"));

        SqlSource sqlSource = sqlSourceMap.get("testChoose");

        String expectedSql = "SELECT\n" +
                "  name,\n" +
                "  category,\n" +
                "  price\n" +
                "FROM\n" +
                "  fruits\n" +
                "WHERE\n" +
                "  name = 'testChoose'";


        Assertions.assertEquals(expectedSql, sqlSource.getSql("testChoose"));

        expectedSql = "SELECT\n" +
                "  name,\n" +
                "  category,\n" +
                "  price\n" +
                "FROM\n" +
                "  fruits\n" +
                "WHERE\n" +
                "  name = 'testBind'";

        Assertions.assertEquals(expectedSql, sqlSource.getSql("{\"name\": \"testBind\"}"));

        Assertions.assertEquals(expectedSql, sqlSource.getSql("{\"name\": \"testBind\", \"category\": \"apple\"}"));

        Assertions.assertEquals(expectedSql, sqlSource.getSql("{\"name\": \"testBind\", \"category\": \"banana\"}"));

        expectedSql = "SELECT\n" +
                "  name,\n" +
                "  category,\n" +
                "  price\n" +
                "FROM\n" +
                "  fruits\n" +
                "WHERE\n" +
                "  category = 'banana'";

        Assertions.assertEquals(expectedSql, sqlSource.getSql("{\"category\": \"banana\"}"));

        expectedSql = "SELECT\n" +
                "  name,\n" +
                "  category,\n" +
                "  price\n" +
                "FROM\n" +
                "  fruits\n" +
                "WHERE\n" +
                "  category = 'banana'\n" +
                "  AND price = 10.00";

        Assertions.assertEquals(expectedSql, sqlSource.getSql("{\"category\": \"banana\", \"price\": 10.00}"));

        expectedSql = "SELECT\n" +
                "  name,\n" +
                "  category,\n" +
                "  price\n" +
                "FROM\n" +
                "  fruits\n" +
                "WHERE\n" +
                "  category = 'apple'";
        Assertions.assertEquals(expectedSql, sqlSource.getSql(null));
    }

    @Test
    public void testAdvancedForeach() {
        XMLMapperBuilder builder = new XMLMapperBuilder(LoadXmlUtil.load("foreach-advanced-fruits.xml"));
        Map<String, SqlSource> sqlSourceMap = builder.parse();
        Assertions.assertNotNull(sqlSourceMap);
        Assertions.assertTrue(sqlSourceMap.containsKey("testInsertMulti"));

        SqlSource sqlSource = sqlSourceMap.get("testInsertMulti");

        String expectedSql = "INSERT INTO\n" +
                "  fruits (name, category, price)\n" +
                "VALUES\n" +
                "  ('Jonathan', 'apple', 10.00)";

        Assertions.assertEquals(expectedSql, sqlSource.getSql("{\"fruits\":[{\"name\": \"Jonathan\", \"price\": 10.00, \"category\": \"apple\"}]}"));

        expectedSql = "INSERT INTO\n" +
                "  fruits (name, category, price)\n" +
                "VALUES\n" +
                "  ('Jonathan', 'apple', 10.00),\n" +
                "  ('Mcintosh', 'apple', 12.00)";

        Assertions.assertEquals(expectedSql, sqlSource.getSql("{\"fruits\":[{\"name\": \"Jonathan\", \"price\": 10.00, \"category\": \"apple\"},{\"name\": \"Mcintosh\", \"price\": 12.00, \"category\": \"apple\"}]}"));

    }

    @Test
    public void testBasicForeach() {
        XMLMapperBuilder builder = new XMLMapperBuilder(LoadXmlUtil.load("foreach-basic-fruits.xml"));
        Map<String, SqlSource> sqlSourceMap = builder.parse();
        Assertions.assertNotNull(sqlSourceMap);
        Assertions.assertTrue(sqlSourceMap.containsKey("testForeach"));

        SqlSource sqlSource = sqlSourceMap.get("testForeach");

        String expectedSql = "SELECT\n" +
                "  name,\n" +
                "  category,\n" +
                "  price\n" +
                "FROM\n" +
                "  fruits\n" +
                "WHERE\n" +
                "  category = 'apple'\n" +
                "  AND (name = 'Jonathan')";

        Assertions.assertEquals(expectedSql, sqlSource.getSql("{\"apples\":[\"Jonathan\", \"Mcintosh\"]}"));

        expectedSql = "SELECT\n" +
                "  name,\n" +
                "  category,\n" +
                "  price\n" +
                "FROM\n" +
                "  fruits\n" +
                "WHERE\n" +
                "  category = 'apple'\n" +
                "  AND (\n" +
                "    name = 'Jonathan'\n" +
                "    OR name = 'Fuji'\n" +
                "  )";

        Assertions.assertEquals(expectedSql, sqlSource.getSql("{\"apples\":[\"Jonathan\",\"Mcintosh\", \"Fuji\"]}"));

        expectedSql = "SELECT\n" +
                "  name,\n" +
                "  category,\n" +
                "  price\n" +
                "FROM\n" +
                "  fruits\n" +
                "WHERE\n" +
                "  category = 'apple'\n" +
                "  AND ()";

        Assertions.assertEquals(expectedSql, sqlSource.getSql("{\"apples\":[\"Mcintosh\"]}"));
    }

    @Test
    public void testIf() {
        XMLMapperBuilder builder = new XMLMapperBuilder(LoadXmlUtil.load("if-fruits.xml"));
        Map<String, SqlSource> sqlSourceMap = builder.parse();
        Assertions.assertNotNull(sqlSourceMap);
        Assertions.assertTrue(sqlSourceMap.containsKey("testIf"));

        SqlSource sqlSource = sqlSourceMap.get("testIf");

        String expectedSql = "SELECT\n" +
                "  name,\n" +
                "  category,\n" +
                "  price\n" +
                "FROM\n" +
                "  fruits\n" +
                "WHERE\n" +
                "  1 = 1\n" +
                "  AND category = 'apple'\n" +
                "  AND price = 100.0";

        Assertions.assertEquals(expectedSql, sqlSource.getSql("{\"price\": 100.0, \"category\": \"apple\"}"));

        expectedSql = "SELECT\n" +
                "  name,\n" +
                "  category,\n" +
                "  price\n" +
                "FROM\n" +
                "  fruits\n" +
                "WHERE\n" +
                "  1 = 1\n" +
                "  AND category = 'apple'\n" +
                "  AND price = 500.0\n" +
                "  AND name = 'Fuji'";

        Assertions.assertEquals(expectedSql, sqlSource.getSql("{\"price\": 500.0, \"category\": \"apple\"}"));
    }

    @Test
    public void testInclude() {
        XMLMapperBuilder builder = new XMLMapperBuilder(LoadXmlUtil.load("include-fruits.xml"));
        Map<String, SqlSource> sqlSourceMap = builder.parse();
        Assertions.assertNotNull(sqlSourceMap);
        Assertions.assertTrue(sqlSourceMap.containsKey("testInclude"));

        SqlSource sqlSource = sqlSourceMap.get("testInclude");

        String expectedSql = "SELECT\n" +
                "  name,\n" +
                "  category,\n" +
                "  price\n" +
                "FROM\n" +
                "  fruits\n" +
                "WHERE\n" +
                "  category = 'apple'";

        Assertions.assertEquals(expectedSql, sqlSource.getSql("apple"));
    }

    @Test
    public void testParameters() {
        XMLMapperBuilder builder = new XMLMapperBuilder(LoadXmlUtil.load("parameters-fruits.xml"));
        Map<String, SqlSource> sqlSourceMap = builder.parse();
        Assertions.assertNotNull(sqlSourceMap);
        Assertions.assertTrue(sqlSourceMap.containsKey("testParameters"));

        SqlSource sqlSource = sqlSourceMap.get("testParameters");

        String expectedSql = "SELECT\n" +
                "  name,\n" +
                "  category,\n" +
                "  price\n" +
                "FROM\n" +
                "  fruits\n" +
                "WHERE\n" +
                "  category = 'apple'\n" +
                "  AND price > 100.00";

        Assertions.assertEquals(expectedSql, sqlSource.getSql("{\"category\": \"apple\", \"price\": 100.00}"));

    }

    @Test
    public void testSet() {
        XMLMapperBuilder builder = new XMLMapperBuilder(LoadXmlUtil.load("set-fruits.xml"));
        Map<String, SqlSource> sqlSourceMap = builder.parse();
        Assertions.assertNotNull(sqlSourceMap);
        Assertions.assertTrue(sqlSourceMap.containsKey("testSet"));

        SqlSource sqlSource = sqlSourceMap.get("testSet");

        String expectedSql = "UPDATE\n" +
                "  fruits\n" +
                "SET\n" +
                "  category = 'apple',\n" +
                "  price = 10.00\n" +
                "WHERE\n" +
                "  name = 'Jonathan'";

        Assertions.assertEquals(expectedSql, sqlSource.getSql("{\"name\": \"Jonathan\", \"price\": 10.00, \"category\": \"apple\"}"));

        expectedSql = "UPDATE\n" +
                "  fruits\n" +
                "SET\n" +
                "  category = 'apple'\n" +
                "WHERE\n" +
                "  name = 'Jonathan'";

        Assertions.assertEquals(expectedSql, sqlSource.getSql("{\"name\": \"Jonathan\", \"category\": \"apple\"}"));

        expectedSql = "UPDATE\n" +
                "  fruits\n" +
                "SET\n" +
                "  price = 10.00\n" +
                "WHERE\n" +
                "  name = 'Jonathan'";

        Assertions.assertEquals(expectedSql, sqlSource.getSql("{\"name\": \"Jonathan\", \"price\": 10.00}"));

    }

    @Test
    public void testTrim() {
        XMLMapperBuilder builder = new XMLMapperBuilder(LoadXmlUtil.load("trim-fruits.xml"));
        Map<String, SqlSource> sqlSourceMap = builder.parse();
        Assertions.assertNotNull(sqlSourceMap);
        Assertions.assertTrue(sqlSourceMap.containsKey("testTrim"));

        SqlSource sqlSource = sqlSourceMap.get("testTrim");

        String expectedSql = "SELECT\n" +
                "  name,\n" +
                "  category,\n" +
                "  price\n" +
                "FROM\n" +
                "  fruits\n" +
                "WHERE\n" +
                "  category = 'apple'\n" +
                "  OR price = 200";

        Assertions.assertEquals(expectedSql, sqlSource.getSql(null));
    }

    @Test
    public void testWhere() {
        XMLMapperBuilder builder = new XMLMapperBuilder(LoadXmlUtil.load("where-fruits.xml"));
        Map<String, SqlSource> sqlSourceMap = builder.parse();
        Assertions.assertNotNull(sqlSourceMap);
        Assertions.assertTrue(sqlSourceMap.containsKey("testWhere"));

        SqlSource sqlSource = sqlSourceMap.get("testWhere");

        String expectedSql = "SELECT\n" +
                "  name,\n" +
                "  category,\n" +
                "  price\n" +
                "FROM\n" +
                "  fruits\n" +
                "WHERE\n" +
                "  category = 'apple'\n" +
                "  AND price = 10.00";

        Assertions.assertEquals(expectedSql, sqlSource.getSql("10.00"));
    }
}