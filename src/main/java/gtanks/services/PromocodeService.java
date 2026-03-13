/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.services;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import gtanks.commands.Type;
import gtanks.lobby.LobbyManager;
import gtanks.logger.Logger;
import gtanks.utils.ResourceUtils;
import gtanks.main.database.DatabaseManager;
import gtanks.main.database.impl.DatabaseManagerImpl;
import gtanks.services.annotations.ServicesInject;
import gtanks.users.garage.GarageItemsLoader;
import gtanks.users.garage.items.Item;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.io.FileWriter;
import java.io.IOException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class PromocodeService {
    private static final String PROMO_CODES_FILE = ResourceUtils.data("json/promocodes.json");
    private static final PromocodeService INSTANCE = new PromocodeService();
    @ServicesInject(target=DatabaseManagerImpl.class)
    private DatabaseManager database = DatabaseManagerImpl.instance();
    private JSONArray promoCodes;

    public PromocodeService() {
        this.loadPromoCodes();
    }

    public void loadPromoCodes() {
        Logger.log("INIT: PromocodeService succesfully");
        JSONParser parser = new JSONParser();
        try (InputStreamReader reader = new InputStreamReader(new FileInputStream(PROMO_CODES_FILE), StandardCharsets.UTF_8);){
            Object obj = parser.parse(reader);
            this.promoCodes = (JSONArray)obj;
        } catch (Exception e) {
            this.promoCodes = new JSONArray();
            Logger.log(gtanks.logger.Type.ERROR, "Failed to load promo codes from " + PROMO_CODES_FILE + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static PromocodeService instance() {
        return INSTANCE;
    }

    public void checkPromoCode(LobbyManager lobby, String message) {
        for (Object promoObj : this.promoCodes) {
            JSONObject promo = (JSONObject)promoObj;
            String promoCode = (String)promo.get("code");
            String promoType = (String)promo.get("type");
            if (!promoCode.equals(message)) continue;
            JSONArray usedByPlayers = (JSONArray)promo.get("usedByPlayers");
            String playerName = lobby.getLocalUser().getNickname();
            if (promoType.equals("unique")) {
                if (!usedByPlayers.contains(playerName)) {
                    this.applyPromoCode(lobby, promo);
                    usedByPlayers.add(playerName);
                    this.savePromoCodes();
                    this.sendTableMessage(lobby, "\u041f\u0440\u043e\u043c\u043e\u043a\u043e\u0434 \u0443\u0441\u043f\u0435\u0448\u043d\u043e \u0430\u043a\u0442\u0438\u0432\u0438\u0440\u043e\u0432\u0430\u043d!");
                    Logger.debug("User " + lobby.getLocalUser().getNickname() + " activated promocode " + promoCode + " type " + promoType);
                } else {
                    this.sendTableMessage(lobby, "\u042d\u0442\u043e\u0442 \u043f\u0440\u043e\u043c\u043e\u043a\u043e\u0434 \u0443\u0436\u0435 \u0431\u044b\u043b \u0438\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u043d \u0432\u0430\u043c\u0438!");
                }
            } else if (promoType.equals("single_use")) {
                if (usedByPlayers.isEmpty()) {
                    this.applyPromoCode(lobby, promo);
                    usedByPlayers.add(playerName);
                    this.savePromoCodes();
                    this.sendTableMessage(lobby, "\u041f\u0440\u043e\u043c\u043e\u043a\u043e\u0434 \u0443\u0441\u043f\u0435\u0448\u043d\u043e \u0430\u043a\u0442\u0438\u0432\u0438\u0440\u043e\u0432\u0430\u043d!");
                    Logger.debug("User " + lobby.getLocalUser().getNickname() + " activated promocode " + promoCode + " type " + promoType);
                } else {
                    this.sendTableMessage(lobby, "\u042d\u0442\u043e\u0442 \u043f\u0440\u043e\u043c\u043e\u043a\u043e\u0434 \u0443\u0436\u0435 \u0431\u044b\u043b \u0438\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u043d!");
                }
            }
            return;
        }
        this.sendTableMessage(lobby, "\u041d\u0435\u0432\u0435\u0440\u043d\u044b\u0439 \u043f\u0440\u043e\u043c\u043e\u043a\u043e\u0434!");
    }

    private void applyPromoCode(LobbyManager lobby, JSONObject promo) {
        JSONObject reward = (JSONObject)promo.get("reward");
        this.activatePromocode(lobby, reward);
    }

    private void savePromoCodes() {
        try (FileWriter file = new FileWriter(PROMO_CODES_FILE);){
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String formattedJson = gson.toJson(this.promoCodes);
            file.write(formattedJson);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void activatePromocode(LobbyManager lobby, JSONObject reward) {
        if (reward.containsKey("crystals")) {
            long crystals = (Long)reward.get("crystals");
            lobby.addCrystall((int)crystals);
        }
        if (reward.containsKey("paint")) {
            String paint = (String)reward.get("paint");
            Item paintItem = lobby.getLocalUser().getGarage().getItemById(paint);
            if (paintItem == null) {
                paintItem = GarageItemsLoader.items.get(paint).clone();
                lobby.getLocalUser().getGarage().items.add(paintItem);
            }
        }
        if (reward.containsKey("gifts")) {
            int gifts = ((Long)reward.get("gifts")).intValue();
            Item giftItem = lobby.getLocalUser().getGarage().getItemById("gift");
            if (giftItem == null) {
                giftItem = GarageItemsLoader.items.get("gift").clone();
                lobby.getLocalUser().getGarage().items.add(giftItem);
            }
            giftItem.count += gifts;
        }
        if (reward.containsKey("turret")) {
            String turret = (String)reward.get("turret");
            Item turretItem = lobby.getLocalUser().getGarage().getItemById(turret);
            if (turretItem == null) {
                turretItem = GarageItemsLoader.items.get(turret).clone();
                lobby.getLocalUser().getGarage().items.add(turretItem);
                lobby.getLocalUser().getGarage().mountItem(turret + "_m3");
            }
        }
        if (reward.containsKey("bonusItems")) {
            JSONObject bonusItems = (JSONObject)reward.get("bonusItems");
            for (Object itemId : bonusItems.keySet()) {
                String bonusItemId = (String)itemId;
                long count = (Long)bonusItems.get(bonusItemId);
                Item bonusItem = lobby.getLocalUser().getGarage().getItemById(bonusItemId);
                if (bonusItem == null) {
                    bonusItem = GarageItemsLoader.items.get(bonusItemId).clone();
                    lobby.getLocalUser().getGarage().items.add(bonusItem);
                }
                bonusItem.count = (int)((long)bonusItem.count + count);
            }
        }
        lobby.getLocalUser().getGarage().parseJSONData();
        this.database.update(lobby.getLocalUser().getGarage());
    }

    public void sendTableMessage(LobbyManager lobby, String msg) {
        lobby.send(Type.LOBBY, "server_message", msg);
    }
}

