package gtanks.services;

import gtanks.commands.Type;
import gtanks.lobby.LobbyManager;
import gtanks.logger.Logger;
import gtanks.main.database.DatabaseManager;
import gtanks.utils.ResourceUtils;
import gtanks.main.database.impl.DatabaseManagerImpl;
import gtanks.services.annotations.ServicesInject;
import gtanks.users.User;
import gtanks.users.garage.GarageItemsLoader;
import gtanks.users.garage.enums.ItemType;
import gtanks.users.garage.items.Item;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class GiftsServices {

    private static final GiftsServices INSTANCE = new GiftsServices();
    @ServicesInject(target = DatabaseManagerImpl.class)
    private DatabaseManager database = DatabaseManagerImpl.instance();
    private List<JSONObject> loadedGifts;

    public static GiftsServices instance() {
        return INSTANCE;
    }

    private GiftsServices() {
        this.loadedGifts = new ArrayList<>();
        this.loadGiftsFromFile();
    }

    private void loadGiftsFromFile() {
        JSONParser parser = new JSONParser();
        try (InputStreamReader reader = new InputStreamReader(new FileInputStream(ResourceUtils.data("json/giftsItems.json")), StandardCharsets.UTF_8)) {
            JSONArray jsonArray = (JSONArray) parser.parse(reader);
            for (Object obj : jsonArray) {
                loadedGifts.add((JSONObject) obj);
            }
        } catch (IOException | ParseException e) {
            Logger.log(gtanks.logger.Type.ERROR, "Error loading gifts from file: " + e.getMessage());
        }
    }

    public void userOnGiftsWindowOpen(LobbyManager lobby) {
        Item giftItem = lobby.getLocalUser().getGarage().getItemById("gift");
        lobby.send(Type.LOBBY, "show_gifts_window", loadedGifts.toString(), String.valueOf(giftItem.count));
    }

    public void tryRollItem(LobbyManager lobby) {
        try {
            JSONObject randomItem = pickRandomItem(loadedGifts);
            String itemId = (String) randomItem.get("item_id");
            int countItems = ((Long) randomItem.get("count")).intValue();
            int rarity = ((Long) randomItem.get("rarity")).intValue();
            String itemName;
            int offsetCrystalls = 0;

            if (itemId.startsWith("set_")) {
                int setItemCount = Integer.parseInt(itemId.substring(4));
                itemName = "Комплект припасов х" + setItemCount;
                addBonusItemsToGarage(lobby.getLocalUser(), setItemCount);
            } else if (itemId.equals("crystalls")) {
                itemName = "Кристаллы x" + countItems;
                lobby.dummyAddCrystall(countItems);
                offsetCrystalls = countItems;
            } else {
                itemName = getItemNameWithCount(lobby, itemId, countItems);
                offsetCrystalls = getOffsetCrystalls(lobby, GarageItemsLoader.items.get(itemId));
                rewardGiftItemToUser(lobby, GarageItemsLoader.items.get(itemId));
            }

            Logger.debug("User " + lobby.getLocalUser().getNickname() + " added item " + itemName + " to garage");
            Item giftItem = lobby.getLocalUser().getGarage().getItemById("gift");
            updateInventory(lobby, giftItem, 1);
            lobby.send(Type.LOBBY, "item_rolled", itemId, countItems + ";" + offsetCrystalls, itemName + ";" + rarity);
        } catch (Exception e) {
            Logger.log(gtanks.logger.Type.ERROR, "Error rolling item: " + e.getMessage());
        }
    }

    public void rollItems(LobbyManager lobby, int rollCount) {
        JSONArray jsonArrayGift = new JSONArray();
        StringBuilder resultLogs = new StringBuilder("[GIFT_SYSTEM_LOG_OUT]: Details:");
        resultLogs.append(" Nickname: ").append(lobby.getLocalUser().getNickname());
        resultLogs.append(" gifts opened count: ").append(rollCount).append("\n");

        try {
            for (int i = 0; i < rollCount; ++i) {
                JSONObject randomItem = pickRandomItem(loadedGifts);
                String itemId = (String) randomItem.get("item_id");
                int countItems = ((Long) randomItem.get("count")).intValue();
                int rarity = ((Long) randomItem.get("rarity")).intValue();
                JSONArray numInventoryCounts = new JSONArray();
                for (int j = 0; j < 5; ++j) {
                    numInventoryCounts.add(0);
                }

                String itemName;
                int offsetCrystalls = 0;

                if (itemId.startsWith("set_")) {
                    int setItemCount = Integer.parseInt(itemId.substring(4));
                    itemName = "Комплект припасов х" + setItemCount;
                    addBonusItemsToGarage(lobby.getLocalUser(), setItemCount);
                } else if (itemId.equals("crystalls")) {
                    itemName = "Кристаллы x" + countItems;
                    lobby.dummyAddCrystall(countItems);
                } else {
                    itemName = getItemNameWithCount(lobby, itemId, countItems);
                    offsetCrystalls = getOffsetCrystalls(lobby, GarageItemsLoader.items.get(itemId));
                    rewardGiftItemToUser(lobby, GarageItemsLoader.items.get(itemId));
                }

                resultLogs.append("===================================\n");
                resultLogs.append("[GIFT_SYSTEM_LOG_OUT]: Prize: ").append(itemName).append("\n");
                JSONObject newItem = new JSONObject();
                newItem.put("itemId", itemId);
                newItem.put("visualItemName", itemName);
                newItem.put("rarity", rarity);
                newItem.put("offsetCrystalls", offsetCrystalls);
                newItem.put("numInventoryCounts", numInventoryCounts);
                jsonArrayGift.add(newItem);
            }
        } catch (Exception e) {
            Logger.log(gtanks.logger.Type.ERROR, "Error rolling items: " + e.getMessage());
        }

        Logger.debug(resultLogs.toString());
        Item giftItem = lobby.getLocalUser().getGarage().getItemById("gift");
        updateInventory(lobby, giftItem, rollCount);
        lobby.send(Type.LOBBY, "items_rolled", jsonArrayGift.toString());
    }

    private void updateInventory(LobbyManager lobby, Item item, int amountToRemove) {
        item.count -= amountToRemove;
        if (item.count <= 0) {
            lobby.getLocalUser().getGarage().items.remove(item);
        }
        lobby.getLocalUser().getGarage().parseJSONData();
        database.update(lobby.getLocalUser().getGarage());
    }

    private String getItemNameWithCount(LobbyManager lobby, String itemId, int countItems) {
        Item item = GarageItemsLoader.items.get(itemId);
        String itemName = item.name.localizatedString(lobby.getLocalUser().getLocalization());
        List<String> specialItemIds = Arrays.asList("mine", "n2o", "health", "armor", "double_damage");

        if (specialItemIds.contains(itemId)) {
            Item bonusItem = lobby.getLocalUser().getGarage().getItemById(itemId);
            if (bonusItem == null) {
                bonusItem = item.clone();
                lobby.getLocalUser().getGarage().items.add(bonusItem);
            }
            bonusItem.count += countItems;
            itemName += " x" + countItems;
        }

        return itemName;
    }

    private void rewardGiftItemToUser(LobbyManager lobby, Item item) {
        boolean containsItem = lobby.getLocalUser().getGarage().containsItem(item.id);
        if (containsItem && item.itemType != ItemType.INVENTORY) {
            lobby.dummyAddCrystall(item.price / 2);
            Logger.log("User " + lobby.getLocalUser().getNickname() + " contains item in garage reward: " + item.price / 2 + " crystals.");
        } else if (!containsItem) {
            lobby.getLocalUser().getGarage().items.add(item);
        }
    }

    private int getOffsetCrystalls(LobbyManager lobby, Item item) {
        boolean containsItem = lobby.getLocalUser().getGarage().containsItem(item.id);
        int offsetCrystalls = 0;
        if (containsItem && item.itemType != ItemType.INVENTORY) {
            offsetCrystalls = item.price / 2;
            Logger.log("User " + lobby.getLocalUser().getNickname() + " offsetCrystalls: " + offsetCrystalls);
        }
        return offsetCrystalls;
    }

    public static JSONObject pickRandomItem(List<JSONObject> jsonArray) {
        Random random = new Random();
        int[] rarityProbabilities = new int[]{50, 34, 10, 5, 1};
        int totalProbabilitySum = Arrays.stream(rarityProbabilities).sum();
        int randomValue = random.nextInt(totalProbabilitySum);
        int cumulativeProbability = 0;
        int rarity = 0;

        for (int i = 0; i < rarityProbabilities.length; ++i) {
            cumulativeProbability += rarityProbabilities[i];
            if (randomValue < cumulativeProbability) {
                rarity = i;
                break;
            }
        }

        List<JSONObject> itemsWithRarity = new ArrayList<>();
        for (JSONObject item : jsonArray) {
            if (((Long) item.get("rarity")).intValue() == rarity) {
                itemsWithRarity.add(item);
            }
        }

        return itemsWithRarity.isEmpty() ? jsonArray.get(0) : itemsWithRarity.get(random.nextInt(itemsWithRarity.size()));
    }

    private void addBonusItemsToGarage(User localUser, int setItemCount) {
        Logger.log("addBonusItemsToGarage()::setItemCount: " + setItemCount);
        List<String> bonusItemIds = Arrays.asList("n2o", "double_damage", "armor", "mine", "health");
        for (String bonusItemId : bonusItemIds) {
            Item bonusItem = localUser.getGarage().getItemById(bonusItemId);
            if (bonusItem == null) {
                bonusItem = GarageItemsLoader.items.get(bonusItemId).clone();
                localUser.getGarage().items.add(bonusItem);
            }
            bonusItem.count += setItemCount;
        }
    }
}
