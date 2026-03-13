/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.users.garage.items;

import gtanks.system.localization.strings.LocalizedString;
import gtanks.users.garage.enums.ItemType;
import gtanks.users.garage.items.PropertyItem;
import gtanks.users.garage.items.modification.ModificationInfo;
import gtanks.utils.StringUtils;

public class Item {
    public boolean multicounted = true;
    public double timeRemaining;
    public String id;
    public LocalizedString description;
    public boolean isInventory;
    public int index;
    public PropertyItem[] propetys;
    public ItemType itemType;
    public int modificationIndex;
    public LocalizedString name;
    public PropertyItem[] nextProperty;
    public int nextPrice;
    public int nextRankId;
    public int price;
    public int rankId;
    public ModificationInfo[] modifications;
    public boolean specialItem;
    public int count;
    private static final long DAY_DURATION_MS = 86400000L;

    public Item(String id, LocalizedString description, boolean isInventory, int index, PropertyItem[] propetys, ItemType weapon, int modificationIndex, LocalizedString name, PropertyItem[] nextProperty, int nextPrice, int nextRankId, int price, int rankId, ModificationInfo[] modifications, boolean specialItem, int count) {
        double currentTimePlusOneMonth;
        long currentTimeMillis;
        this.id = id;
        this.description = description;
        this.isInventory = isInventory;
        this.index = index;
        this.propetys = propetys;
        this.itemType = weapon;
        this.modificationIndex = modificationIndex;
        this.name = name;
        this.nextProperty = nextProperty;
        this.nextPrice = nextPrice;
        this.nextRankId = nextRankId;
        this.price = price;
        this.rankId = rankId;
        this.modifications = modifications;
        this.specialItem = specialItem;
        this.count = count;
        if (id.equals("up_score_start")) {
            double currentTimePlusOneWeek;
            currentTimeMillis = System.currentTimeMillis();
            this.timeRemaining = currentTimePlusOneWeek = (double)(currentTimeMillis + 604800000L);
        }
        if (id.equals("up_score")) {
            currentTimeMillis = System.currentTimeMillis();
            this.timeRemaining = currentTimePlusOneMonth = (double)(currentTimeMillis + 2592000000L);
        }
        if (id.equals("no_supplies")) {
            currentTimeMillis = System.currentTimeMillis();
            this.timeRemaining = currentTimePlusOneMonth = (double)(currentTimeMillis + 2592000000L);
        }
    }

    public String getId() {
        return StringUtils.concatStrings(this.id, "_m", String.valueOf(this.modificationIndex));
    }

    public void setRemainingDurationForObtainedItem() {
        if (this.id.equals("no_supplies") || this.id.equals("double_crystalls")) {
            this.timeRemaining = System.currentTimeMillis() + DAY_DURATION_MS;
        }
    }

    public void setRemainingDurationDays(int days) {
        int safeDays = Math.max(1, days);
        this.timeRemaining = System.currentTimeMillis() + DAY_DURATION_MS * (long)safeDays;
    }

    public Item clone() {
        return new Item(this.id, this.description, this.isInventory, this.index, this.propetys, this.itemType, this.modificationIndex, this.name, this.nextProperty, this.nextPrice, this.nextRankId, this.price, this.rankId, this.modifications, this.specialItem, this.count);
    }
}
