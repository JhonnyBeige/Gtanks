/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.battles.maps.parser.map.bonus;

public class BonusType {
    public static final BonusType NITRO = new BonusType("nitro");
    public static final BonusType DAMAGE = new BonusType("damageup");
    public static final BonusType ARMOR = new BonusType("armorup");
    public static final BonusType HEAL = new BonusType("medkit");
    public static final BonusType CRYSTALL = new BonusType("crystal");
    public static final BonusType CRYSTALL_100 = new BonusType("crystal_100");
    private String value;

    private BonusType(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    public static BonusType getType(String value) {
        if (value.equals("medkit")) {
            return HEAL;
        }
        if (value.equals("armorup")) {
            return ARMOR;
        }
        if (value.equals("damageup")) {
            return DAMAGE;
        }
        if (value.equals("nitro")) {
            return NITRO;
        }
        if (value.equals("crystal")) {
            return CRYSTALL;
        }
        return value.equals("crystal_100") ? CRYSTALL_100 : null;
    }
}

