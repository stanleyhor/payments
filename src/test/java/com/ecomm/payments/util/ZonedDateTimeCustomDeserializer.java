package com.ecomm.payments.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import org.apache.logging.log4j.util.Strings;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ZonedDateTimeCustomDeserializer extends JsonDeserializer<ZonedDateTime> {

    private static final String REGEX = "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}[-+]\\d{2}:\\d{2}";
    private static final String OFFSET_REGEX = "[-+]\\d{2}:\\d{2}";

    private static final String DATE_PATTERN_REGEX = "\\d{13}";

    @Override
    public ZonedDateTime deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        String date = parser.getText();
        ZoneId zone = ZoneId.systemDefault();
        LocalDateTime localDateTime;
        Pattern pattern = Pattern.compile(OFFSET_REGEX);
        Matcher matcher = pattern.matcher(date);
        boolean matchFound = matcher.find();
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        if (Strings.isBlank(date)) {
            return null;
        }
        if (date.matches(REGEX)
                && matchFound) {
            String offset = matcher.group();
            int endIndex = date.lastIndexOf(offset);
            zone = ZoneOffset.of(offset);
            String dateWithoutTimezone = date.substring(0, endIndex);
            localDateTime = LocalDateTime.parse(dateWithoutTimezone, formatter);

        } else if (date.matches(DATE_PATTERN_REGEX)) {
            Date dateFromString = new Date(Long.parseLong(date));
            localDateTime = LocalDateTime.ofInstant(dateFromString.toInstant(), zone);
        } else if (matchFound) {

            ZonedDateTime zonedDateTime = ZonedDateTime.parse(date);
            localDateTime = zonedDateTime.toLocalDateTime();
            zone = zonedDateTime.getZone();
        } else {
            localDateTime = new LocalDateTimeDeserializer(formatter).deserialize(parser, context);
        }

        return localDateTime.atZone(zone);
    }

}
