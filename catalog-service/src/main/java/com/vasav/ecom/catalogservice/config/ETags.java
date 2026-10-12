package com.vasav.ecom.catalogservice.config;

import com.vasav.ecom.catalogservice.exception.InvalidPreconditionException;
import com.vasav.ecom.catalogservice.exception.PreconditionRequiredException;

public final class ETags {

    private ETags() {
    }

    public static String of(Long version) {
        return "\"" + version + "\"";
    }

    public static long parse(String ifMatch) {
        if (ifMatch == null) {
            throw new PreconditionRequiredException("Send If-Match: \"<version>\" to modify this resource");
        }
        try {
            return Long.parseLong(ifMatch.replace("W/", "").replace("\"", "").trim());
        } catch (NumberFormatException e) {
            throw new InvalidPreconditionException("Invalid If-Match: " + ifMatch);
        }
    }
}
