package de.bas.contentsync.beans;

import java.util.Arrays;

/**
 * @author mschwarz
 */
public enum RepeatEvery {
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
