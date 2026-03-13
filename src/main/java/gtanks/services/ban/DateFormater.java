/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.services.ban;

import java.util.concurrent.TimeUnit;

public class DateFormater {
    public static String formatTimeToUnban(long millis) {
        String hours = DateFormater.format(String.valueOf(TimeUnit.MILLISECONDS.toHours(millis)));
        String minutes = DateFormater.format(String.valueOf(TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis))));
        return hours + " \u0447\u0430\u0441\u0430(\u043e\u0432) " + minutes + " \u043c\u0438\u043d\u0443\u0442(\u044b)";
    }

    private static String format(String src) {
        if (src.startsWith("-")) {
            src = src.substring(1);
        }
        return src;
    }
}

