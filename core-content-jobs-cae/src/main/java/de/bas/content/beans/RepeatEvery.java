package de.bas.content.beans;

import java.util.Arrays;

/**
 * @author mschwarz
 */
public enum RepeatEvery {
    MINUTE, // for testing
    HOUR,
    DAY,
    WEEK;

    public static RepeatEvery get(String s) {
        return Arrays.stream(RepeatEvery.values())
            .filter(repeatEvery -> repeatEvery.name().equals(s))
            .findFirst()
            .orElse(null);
    }
}
