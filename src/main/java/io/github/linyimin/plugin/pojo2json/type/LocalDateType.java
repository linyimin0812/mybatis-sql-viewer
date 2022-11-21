package io.github.linyimin.plugin.pojo2json.type;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * @author banzhe
 * @date 2022/11/21 20:33
 **/
public class LocalDateType extends TemporalType implements SpecifyType {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");


    @Override
    public Object def() {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli((long) super.def()), ZoneId.systemDefault());
        return localDateTime.toLocalDate().format(formatter);
    }

    @Override
    public Object random() {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli((long) super.random()), ZoneId.systemDefault());
        return localDateTime.toLocalDate().format(formatter);
    }
}
