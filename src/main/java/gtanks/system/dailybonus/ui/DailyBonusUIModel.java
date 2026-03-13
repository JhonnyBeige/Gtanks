/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.system.dailybonus.ui;

import gtanks.commands.Type;
import gtanks.lobby.LobbyManager;
import gtanks.system.dailybonus.BonusListItem;
import gtanks.users.garage.GarageItemsLoader;
import gtanks.users.garage.items.Item;
import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class DailyBonusUIModel {
    public void showBonuses(LobbyManager lobby, List<BonusListItem> bonusesData) {
        JSONObject json = new JSONObject();
        JSONArray items = new JSONArray();
        for (BonusListItem item : bonusesData) {
            JSONObject _item = new JSONObject();
            _item.put("id", item.getBonus().id);
            _item.put("count", item.getCount());
            items.add(_item);
        }
        json.put("items", items);
        lobby.send(Type.LOBBY, "show_bonuses", json.toJSONString());
    }

    public void showCrystalls(LobbyManager lobby, int count) {
        lobby.send(Type.LOBBY, "show_crystalls", String.valueOf(count));
    }

    public void showNoSupplies(LobbyManager lobby) {
        Item noSuppliesItem = GarageItemsLoader.items.get("no_supplies");
        noSuppliesItem.setRemainingDurationForObtainedItem();
        lobby.send(Type.LOBBY, "show_no_supplies");
        lobby.getLocalUser().getGarage().items.add(noSuppliesItem);
    }
}

