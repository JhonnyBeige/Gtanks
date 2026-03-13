/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.system.dailybonus;

import gtanks.users.garage.items.Item;

public class BonusListItem {
    private Item bonus;
    private int count;

    public BonusListItem(Item bonus, int count) {
        this.bonus = bonus;
        this.count = count;
    }

    public Item getBonus() {
        return this.bonus;
    }

    public void setBonus(Item bonus) {
        this.bonus = bonus;
    }

    public int getCount() {
        return this.count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void addCount(int count) {
        this.count += count;
    }
}

