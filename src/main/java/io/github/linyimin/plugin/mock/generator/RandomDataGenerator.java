package io.github.linyimin.plugin.mock.generator;

import com.intellij.openapi.project.Project;
import io.github.linyimin.plugin.mock.enums.MockRandomParamTypeEnum;
import io.github.linyimin.plugin.mock.schema.Field;
import net.datafaker.Faker;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;

import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @author banzhe
 * @date 2022/11/30 16:16
 **/
public class RandomDataGenerator implements DataGenerator {

    private final Faker[] FAKERS = new Faker[] {new Faker(new Locale("zh-CN")), new Faker()};

    private final static DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public List<Object> generate(Project project, Field field, int rows) {

        List<Object> list = new ArrayList<>(rows);
        for (int i = 0; i < rows; i++) {
            list.add(getRandomValue(field));
        }

        return list;
    }

    private Object getRandomValue(Field field) {

        Faker faker = FAKERS[RandomUtils.nextInt(0, 2)];

        MockRandomParamTypeEnum randomType = MockRandomParamTypeEnum.resolve(field.getMockParam());

        int length = field.getFieldLength() <= 0 ? 6 : field.getFieldLength();

        // TODO: 添加长度
        String defaultValue = RandomStringUtils.randomAlphanumeric(1, length);
        if (randomType == null) {
            return defaultValue;
        }

        if (randomType == MockRandomParamTypeEnum.name) {
            return faker.name().name();
        }

        if (randomType == MockRandomParamTypeEnum.city) {
            return faker.address().city();
        }

        if (randomType == MockRandomParamTypeEnum.email) {
            return faker.internet().emailAddress();
        }

        if (randomType == MockRandomParamTypeEnum.url) {
            return faker.internet().url();
        }

        if (randomType == MockRandomParamTypeEnum.ip) {
            return faker.internet().ipV4Address();
        }

        if (randomType == MockRandomParamTypeEnum.integer) {
            return faker.number().randomNumber();
        }

        if (randomType == MockRandomParamTypeEnum.decimal) {
            return RandomUtils.nextFloat(0, Long.MAX_VALUE);
        }

        if (randomType == MockRandomParamTypeEnum.university) {
            return faker.university().name();
        }

        if (randomType == MockRandomParamTypeEnum.date) {
            return faker.date()
                    .between(Timestamp.valueOf("2022-01-01 00:00:00"), Timestamp.valueOf("2023-01-01 00:00:00"))
                    .toLocalDateTime().format(DATE_TIME_FORMATTER);
        }

        if (randomType == MockRandomParamTypeEnum.timestamp) {
            return faker.date()
                    .between(Timestamp.valueOf("2022-01-01 00:00:00"), Timestamp.valueOf("2023-01-01 00:00:00"))
                    .getTime();
        }

        if (randomType == MockRandomParamTypeEnum.phone) {
            return faker.phoneNumber().cellPhone();
        }

        return defaultValue;

    }
}
