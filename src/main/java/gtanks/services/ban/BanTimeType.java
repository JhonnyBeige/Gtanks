/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.services.ban;

import gtanks.utils.StringUtils;

public class BanTimeType {
    public static final BanTimeType FIVE_MINUTES = new BanTimeType("\u041d\u0410 5 \u041c\u0418\u041d\u0423\u0422.", 12, 5);
    public static final BanTimeType ONE_HOUR = new BanTimeType("\u041d\u0410 1 \u0427\u0410\u0421.", 10, 1);
    public static final BanTimeType ONE_DAY = new BanTimeType("\u041d\u0410 1 \u0414\u0415\u041d\u042c.", 5, 1);
    public static final BanTimeType ONE_WEEK = new BanTimeType("\u041d\u0410 1 \u041d\u0415\u0414\u0415\u041b\u042e.", 4, 1);
    public static final BanTimeType ONE_MONTH = new BanTimeType("\u041d\u0410 1 \u041c\u0415\u0421\u042f\u0426.", 2, 1);
    public static final BanTimeType HALF_YEAR = new BanTimeType("\u041d\u0410 \u041f\u041e\u041b \u0413\u041e\u0414\u0410.", 2, 6);
    public static final BanTimeType FOREVER = new BanTimeType("\u041d\u0410\u0412\u0421\u0415\u0413\u0414\u0410.", 1, 2);
    private final String nameType;
    private int field;
    private int amount;

    private BanTimeType(String nameType, int field, int amount) {
        this.nameType = nameType;
        this.field = field;
        this.amount = amount;
    }

    public static String getConstantName(BanTimeType type) {
        if (type == FIVE_MINUTES) {
            return "FIVE_MINUTES";
        }
        if (type == ONE_HOUR) {
            return "ONE_HOUR";
        }
        if (type == ONE_DAY) {
            return "ONE_DAY";
        }
        if (type == ONE_WEEK) {
            return "ONE_WEEK";
        }
        if (type == ONE_MONTH) {
            return "ONE_MONTH";
        }
        if (type == HALF_YEAR) {
            return "HALF_YEAR";
        }
        if (type == FOREVER) {
            return "FOREVER";
        }
        return null;
    }

    public String getNameType() {
        return this.nameType;
    }

    public int getField() {
        return this.field;
    }

    public int getAmount() {
        return this.amount;
    }

    public String toString() {
        return StringUtils.concatStrings("BanTimeType [", this.nameType, "]");
    }

    public boolean equals(Object obj) {
        BanTimeType _obj;
        try {
            _obj = (BanTimeType)obj;
        } catch (Exception var4) {
            return false;
        }
        return this.getNameType().equals(_obj.getNameType());
    }
}

