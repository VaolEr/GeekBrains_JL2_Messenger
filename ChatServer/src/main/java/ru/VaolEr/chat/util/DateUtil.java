package ru.VaolEr.chat.util;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateUtil {

    public static final String DATE_FORMAT = "dd.MM.yyyy";

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT);

    public static String formatDate(LocalDate date) {
        return DATE_TIME_FORMATTER.format(date);
    }

    public static LocalDate parseDate(String date) throws DateTimeParseException {
        return LocalDate.parse(date, DATE_TIME_FORMATTER);
    }

    public static String getCurrentLocalTime(){
        LocalTime time = LocalTime.now();

        String hours    = (time.getHour() < 10) ? "0" + time.getHour() : String.valueOf(time.getHour());
        String minutes  = (time.getMinute() < 10) ? "0" + time.getMinute() : String.valueOf(time.getMinute());

        return hours + ":" + minutes;
    }
}
