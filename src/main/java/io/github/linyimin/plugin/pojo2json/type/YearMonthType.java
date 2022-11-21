package io.github.linyimin.plugin.pojo2json.type;

import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author banzhe
 * @date 2022/11/21 20:48
 **/
public class YearMonthType extends TemporalType implements SpecifyType {

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");

    @Override
    public Object def() {
        return YearMonth.from(ZonedDateTime.ofInstant(Instant.ofEpochMilli((long) super.def()), ZoneId.systemDefault())).format(formatter);
    }

    @Override
    public Object random() {
        return YearMonth.from(ZonedDateTime.ofInstant(Instant.ofEpochMilli((long) super.random()), ZoneId.systemDefault())).format(formatter);    }
}
