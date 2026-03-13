/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.services.ban;

import gtanks.services.ban.BanTimeType;

public class BanChatCommads {
    public static BanTimeType getTimeType(String cmd) {
        BanTimeType time = null;
        switch (cmd) {
            case "banminutes": {
                time = BanTimeType.FIVE_MINUTES;
                break;
            }
            case "banhour": {
                time = BanTimeType.ONE_HOUR;
                break;
            }
            case "banday": {
                time = BanTimeType.ONE_DAY;
                break;
            }
            case "banweek": {
                time = BanTimeType.ONE_WEEK;
                break;
            }
            case "banmonth": {
                time = BanTimeType.ONE_MONTH;
                break;
            }
            case "banhalfyear": {
                time = BanTimeType.HALF_YEAR;
                break;
            }
            case "banforever": {
                time = BanTimeType.FOREVER;
            }
        }
        return time;
    }
}

