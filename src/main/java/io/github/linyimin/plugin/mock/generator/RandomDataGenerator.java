package io.github.linyimin.plugin.mock.generator;

import com.intellij.openapi.project.Project;
import io.github.linyimin.plugin.mock.enums.MockRandomParamTypeEnum;
import io.github.linyimin.plugin.mock.schema.Field;
import net.datafaker.Faker;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * @author banzhe
 * @date 2022/11/30 16:16
 **/
public class RandomDataGenerator implements DataGenerator {

    private final Faker[] FAKERS = new Faker[] {new Faker(new Locale("zh-CN")), new Faker()};

    private final static DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final static DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public Object generate(Project project, Field field) {

        return getRandomValue(field);
    }

    private Object getRandomValue(Field field) {

        Faker faker = FAKERS[RandomUtils.nextInt(0, 2)];

        MockRandomParamTypeEnum randomType = MockRandomParamTypeEnum.resolve(field.getMockParam());

        int length = field.getFieldLength() <= 0 ? 6 : field.getFieldLength();

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

        if (randomType == MockRandomParamTypeEnum.datetime) {
            return generateDateTime(faker, DATE_TIME_FORMATTER);
        }

        if (randomType == MockRandomParamTypeEnum.date) {
            return generateDateTime(faker, DATE_FORMATTER);
        }

        if (randomType == MockRandomParamTypeEnum.year) {
            return generateDateTime(faker, DateTimeFormatter.ofPattern("yyyy"));
        }

        if (randomType == MockRandomParamTypeEnum.time) {
            return generateDateTime(faker, DateTimeFormatter.ofPattern("HH:mm:ss"));
        }

        if (randomType == MockRandomParamTypeEnum.timestamp) {

            LocalDateTime now = LocalDateTime.now();
            String begin = now.minusYears(10).atZone(ZoneId.systemDefault()).format(DATE_TIME_FORMATTER);
            String end = now.plusYears(10).atZone(ZoneId.systemDefault()).format(DATE_TIME_FORMATTER);

            return faker.date()
                    .between(Timestamp.valueOf(begin), Timestamp.valueOf(end))
                    .getTime();
        }

        if (randomType == MockRandomParamTypeEnum.phone) {
            return faker.phoneNumber().cellPhone();
        }

        return defaultValue;

    }

    private String generateDateTime(Faker faker, DateTimeFormatter formatter) {
        LocalDateTime now = LocalDateTime.now();
        String begin = now.minusYears(10).atZone(ZoneId.systemDefault()).format(formatter);
        String end = now.plusYears(10).atZone(ZoneId.systemDefault()).format(formatter);

        return faker.date()
                .between(Timestamp.valueOf(begin), Timestamp.valueOf(end))
                .toLocalDateTime().format(formatter);
    }
}
