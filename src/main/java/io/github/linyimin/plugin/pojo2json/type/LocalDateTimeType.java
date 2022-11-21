package io.github.linyimin.plugin.pojo2json.type;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * @author banzhe
 * @date 2022/11/21 20:30
 **/
public class LocalDateTimeType extends TemporalType implements SpecifyType {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public Object def() {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli((long) super.def()), ZoneId.systemDefault()).format(formatter);
    }

    @Override
    public Object random() {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli((long) super.random()), ZoneId.systemDefault()).format(formatter);
    }
}
