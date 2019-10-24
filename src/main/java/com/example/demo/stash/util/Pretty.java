package com.example.demo.stash.util;

import java.util.List;

public class Pretty {
    public static String toString(List list) {
        StringBuilder builder = new StringBuilder("[\n");
        for (Object obj : list) {
            builder.append("  ")
                    .append(obj.toString())
                    .append("\n");
        }
        return builder.append("]").toString();
    }
}
