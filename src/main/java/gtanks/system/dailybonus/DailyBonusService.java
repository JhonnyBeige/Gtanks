/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.system.dailybonus;

import gtanks.lobby.LobbyManager;
import gtanks.main.database.DatabaseManager;
import gtanks.main.database.impl.DatabaseManagerImpl;
import gtanks.services.annotations.ServicesInject;
import gtanks.system.dailybonus.BonusListItem;
import gtanks.system.dailybonus.crystalls.CrystallsBonusModel;
import gtanks.system.dailybonus.ui.DailyBonusUIModel;
import gtanks.users.User;
import gtanks.users.garage.Garage;
import gtanks.users.garage.GarageItemsLoader;
import gtanks.users.garage.items.Item;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class DailyBonusService {
    private static final DailyBonusService instance = new DailyBonusService();
    @ServicesInject(target=DatabaseManager.class)
    private static final DatabaseManager databaseManager = DatabaseManagerImpl.instance();
    public static final String DAILY_PASS_ID = "no_supplies";
    public static final String[] SUPPLIES_IDS = new String[]{"armor", "double_damage", "n2o"};
    private static final Map<LobbyManager, Data> waitingUsers = new HashMap<LobbyManager, Data>();
    private static final DailyBonusUIModel uiModel = new DailyBonusUIModel();
    private static final CrystallsBonusModel crystallsBonus = new CrystallsBonusModel();
    private static final Random random = new Random();

    public static DailyBonusService instance() {
        return instance;
    }

    public void userInited(LobbyManager lobby) {
        int fund;
        User user = lobby.getLocalUser();
        if (user.getRang() + 1 > 2 && this.canGetBonus(user) && (fund = (int)(((double)(user.getRang() + 1) - 1.75) * 2.4) * 5) > 0) {
            Data bonusData = new Data(this);
            List<BonusListItem> bonusList = bonusData.bonusList;
            int rankFirstAid = GarageItemsLoader.items.get((Object)"health").rankId;
            int itemCrystalPrice = GarageItemsLoader.items.get((Object)"health").price;
            int countFirstAid = fund / itemCrystalPrice / 2;
            itemCrystalPrice = GarageItemsLoader.items.get((Object)"mine").price;
            int countMine = fund / itemCrystalPrice / 2;
            int rankMine = GarageItemsLoader.items.get((Object)"mine").rankId;
            if ((double)random.nextFloat() < 0.1) {
                bonusData.type = random.nextInt(2) + 1;
            } else {
                int nextInt;
                int count;
                int price;
                Item bonus;
                bonusData.type = 3;
                if ((double)random.nextFloat() < 0.3 && countFirstAid > 0 && user.getRang() >= rankFirstAid) {
                    bonus = GarageItemsLoader.items.get("health");
                    price = bonus.price;
                    count = fund / price / 2 + 1;
                } else if ((double)random.nextFloat() < 0.3 && countMine > 0 && user.getRang() >= rankMine) {
                    bonus = GarageItemsLoader.items.get("mine");
                    price = bonus.price;
                    count = fund / price / 2 + 1;
                } else {
                    nextInt = random.nextInt(3);
                    bonus = GarageItemsLoader.items.get(SUPPLIES_IDS[nextInt]);
                    price = bonus.price;
                    count = fund / price / 2;
                }
                bonusList.add(new BonusListItem(bonus, count));
                fund -= price * count;
                nextInt = random.nextInt(3);
                bonus = GarageItemsLoader.items.get(SUPPLIES_IDS[nextInt]);
                price = bonus.price;
                if (bonusList.get(0).getBonus().equals(bonus)) {
                    bonusList.get(0).addCount(fund / price);
                } else {
                    bonusList.add(new BonusListItem(bonus, fund / price));
                }
            }
            waitingUsers.put(lobby, bonusData);
            Garage garage = user.getGarage();
            for (BonusListItem item : bonusList) {
                Item bonusItem = garage.getItemById(item.getBonus().id);
                if (bonusItem == null) {
                    bonusItem = GarageItemsLoader.items.get(item.getBonus().id).clone();
                    garage.items.add(bonusItem);
                }
                bonusItem.count += item.getCount();
            }
            garage.parseJSONData();
            databaseManager.update(garage);
        }
    }

    public void userLoaded(LobbyManager lobby) {
        Data data = waitingUsers.get(lobby);
        if (data == null) {
            return;
        }
        if (data.type == 1) {
            crystallsBonus.applyBonus(lobby);
            uiModel.showCrystalls(lobby, crystallsBonus.getBonus(lobby.getLocalUser().getRang()));
        } else if (data.type == 2 && !lobby.getLocalUser().getGarage().containsItem(DAILY_PASS_ID)) {
            uiModel.showNoSupplies(lobby);
            lobby.getLocalUser().getGarage().parseJSONData();
            databaseManager.update(lobby.getLocalUser().getGarage());
        } else if (data.type == 3) {
            uiModel.showBonuses(lobby, data.bonusList);
        }
        waitingUsers.remove(lobby);
        this.saveLastDate(lobby.getLocalUser());
    }

    public boolean canGetBonus(User user) {
        if (user == null) {
            return false;
        }
        boolean result = false;
        Date lastDate = user.getLastIssueBonus();
        Date now = new Date(System.currentTimeMillis() - 14400000L);
        Calendar nowCal = Calendar.getInstance();
        nowCal.setTime(now);
        Calendar lastCal = Calendar.getInstance();
        if (lastDate != null) {
            lastCal.setTime(lastDate);
        }
        if (lastDate == null || nowCal.get(5) > lastCal.get(5) || nowCal.get(2) > lastCal.get(2)) {
            result = true;
        }
        return result;
    }

    private void saveLastDate(User user) {
        Date now = new Date(System.currentTimeMillis() - 14400000L);
        user.setLastIssueBonus(now);
        databaseManager.update(user);
    }

    private class Data {
        public int type = 0;
        public List<BonusListItem> bonusList = new ArrayList<BonusListItem>();

        private Data(DailyBonusService dailyBonusService) {
        }
    }
}

