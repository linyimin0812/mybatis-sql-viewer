package io.github.linyimin.plugin.pojo2json.type;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * @author banzhe
 * @date 2022/11/21 20:42
 **/
public class LocalTimeType extends TemporalType implements SpecifyType {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    @Override
    public Object def() {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli((long) super.def()), ZoneId.systemDefault());
        return localDateTime.toLocalTime().format(formatter);
    }

    @Override
    public Object random() {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli((long) super.random()), ZoneId.systemDefault());
        return localDateTime.toLocalTime().format(formatter);
    }
}
