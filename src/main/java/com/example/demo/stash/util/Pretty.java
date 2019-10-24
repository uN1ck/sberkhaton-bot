package com.example.demo.stash.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;

public class Pretty {
    public static String toString(List list) {
        StringBuilder builder = new StringBuilder();
        for (Object obj : list) {
            builder.append(obj.toString())
                    .append("\n");
        }
        return builder.toString();
    }

    public static String toString(boolean val) {
        return val ? "Да" : "Нет";
    }

    public static String toString(LocalDateTime localDateTime) {
        return localDateTime.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT));
    }
}
