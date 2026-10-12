package com.vasav.ecom.catalogservice.config;

public final class ETags {

    private ETags() {
    }

    public static String of(Long version) {
        return "\"" + version + "\"";
    }

    public static long parse(String ifMatch) {
        if (ifMatch == null) {
            throw new RuntimeException();
        }
        try {
            return Long.parseLong(ifMatch.replace("W/", "").replace("\"", "").trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid If-Match: " + ifMatch);
        }
    }
}