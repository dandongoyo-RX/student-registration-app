package com.example.StudentRegistration.country;

import java.text.Collator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * The list of countries a student's nationality can be chosen from.
 * Built from Java's own country data, so it needs no extra library or upkeep.
 */
public final class Countries {

    /** Lower-case name -> proper name, e.g. "ghana" -> "Ghana". Sorted A-Z. */
    private static final Map<String, String> BY_LOWER_NAME = new TreeMap<>();

    static {
        for (String code : Locale.getISOCountries()) {
            Locale locale = new Locale.Builder().setRegion(code).build();
            String name = locale.getDisplayCountry(Locale.ENGLISH);
            if (!name.isBlank()) {
                BY_LOWER_NAME.put(name.toLowerCase(Locale.ROOT), name);
            }
        }
    }

    private Countries() {
    }

    /** All country names, A-Z. */
    public static List<String> names() {
        return BY_LOWER_NAME.values().stream().sorted(Collator.getInstance(Locale.ENGLISH)).toList();
    }

    /** The proper spelling of a country name, or null if it isn't a known country. */
    public static String canonical(String name) {
        if (name == null) {
            return null;
        }
        return BY_LOWER_NAME.get(name.trim().toLowerCase(Locale.ROOT));
    }
}
