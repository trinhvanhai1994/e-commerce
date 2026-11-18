package com.dragun.ecommerce.util;

import java.util.regex.Pattern;

/** Vietnamese mobile numbers, stored as 0 + 9 digits so one person cannot hold two accounts. */
public final class PhoneNumbers {

    private static final Pattern SEPARATORS = Pattern.compile("[\\s.\\-()]");
    // Current mobile prefixes: 03x, 05x, 07x, 08x, 09x
    private static final Pattern MOBILE = Pattern.compile("^0[35789][0-9]{8}$");

    private PhoneNumbers() {
    }

    /**
     * Accepts "0912 345 678", "+84912345678", "84912345678", "0912.345.678".
     * @return normalized number, or null when it is not a Vietnamese mobile number
     */
    public static String normalize(String raw) {
        if (raw == null) {
            return null;
        }
        String s = SEPARATORS.matcher(raw.trim()).replaceAll("");
        if (s.startsWith("+84")) {
            s = "0" + s.substring(3);
        } else if (s.startsWith("84") && s.length() == 11) {
            s = "0" + s.substring(2);
        }
        return MOBILE.matcher(s).matches() ? s : null;
    }
}
