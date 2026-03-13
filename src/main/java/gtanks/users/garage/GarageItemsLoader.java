/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.users.garage;

import gtanks.battles.tanks.colormaps.Colormap;
import gtanks.battles.tanks.colormaps.ColormapsFactory;
import gtanks.system.localization.strings.LocalizedString;
import gtanks.system.localization.strings.StringsLocalizationBundle;
import gtanks.users.TypeUser;
import gtanks.users.garage.enums.ItemType;
import gtanks.users.garage.enums.PropertyType;
import gtanks.users.garage.items.Item;
import gtanks.users.garage.items.PropertyItem;
import gtanks.users.garage.items.modification.ModificationInfo;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class GarageItemsLoader {
    public static HashMap<String, Item> items;
    public static HashMap<String, HashSet<String>> specialItemGroups;
    private static int index;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void loadFromConfig(String turrets, String hulls, String colormaps, String specialTurrets, String specialHulls, String specialColormaps, String inventory, String effects) throws Throwable {
        if (items == null) {
            items = new HashMap();
        }
        if (specialItemGroups == null) {
            specialItemGroups = new HashMap();
        }
        specialItemGroups.clear();
        for (int i = 0; i < 8; ++i) {
            StringBuilder builder = new StringBuilder();
            try {
                Throwable var7 = null;
                Object var8 = null;
                try (BufferedReader reader = new BufferedReader(new InputStreamReader((InputStream)new FileInputStream(new File(i == 0 ? inventory : (i == 1 ? turrets : (i == 2 ? hulls : (i == 3 ? colormaps : (i == 4 ? specialTurrets : (i == 5 ? specialHulls : (i == 6 ? specialColormaps : effects)))))))), StandardCharsets.UTF_8));){
                    String line;
                    while ((line = reader.readLine()) != null) {
                        builder.append(line);
                    }
                } catch (Throwable var16) {
                    if (var7 == null) {
                        var7 = var16;
                    } else if (var7 != var16) {
                        var7.addSuppressed(var16);
                    }
                    throw var7;
                }
            } catch (IOException var17) {
                var17.printStackTrace();
            }
            GarageItemsLoader.parseAndInitItems(builder.toString(), i == 0 ? ItemType.INVENTORY : (i == 1 || i == 4 ? ItemType.WEAPON : (i == 2 || i == 5 ? ItemType.ARMOR : (i == 3 || i == 6 ? ItemType.COLOR : ItemType.PLUGIN))));
        }
    }

    private static void parseAndInitItems(String json, ItemType typeItem) {
        JSONParser parser = new JSONParser();
        try {
            Object obj = parser.parse(json);
            JSONObject jparser = (JSONObject)obj;
            JSONArray jarray = (JSONArray)jparser.get("items");
            for (int i = 0; i < jarray.size(); ++i) {
                JSONObject item = (JSONObject)jarray.get(i);
                LocalizedString name = StringsLocalizationBundle.registerString((String)item.get("name_ru"), (String)item.get("name_en"));
                LocalizedString description = StringsLocalizationBundle.registerString((String)item.get("description_ru"), (String)item.get("description_en"));
                String id = (String)item.get("id");
                if (id.equals("1000_scores")) {
                    index = 0;
                    typeItem = ItemType.INVENTORY;
                }
                if (id.equals("gift")) {
                    typeItem = ItemType.INVENTORY;
                }
                int priceM0 = Integer.parseInt((String)item.get("price_m0"));
                int priceM1 = typeItem != ItemType.COLOR && typeItem != ItemType.INVENTORY && typeItem != ItemType.PLUGIN ? Integer.parseInt((String)item.get("price_m1")) : priceM0;
                int priceM2 = typeItem != ItemType.COLOR && typeItem != ItemType.INVENTORY && typeItem != ItemType.PLUGIN ? Integer.parseInt((String)item.get("price_m2")) : priceM0;
                int priceM3 = typeItem != ItemType.COLOR && typeItem != ItemType.INVENTORY && typeItem != ItemType.PLUGIN ? Integer.parseInt((String)item.get("price_m3")) : priceM0;
                int rangM0 = Integer.parseInt((String)item.get("rang_m0"));
                int rangM1 = typeItem != ItemType.COLOR && typeItem != ItemType.INVENTORY && typeItem != ItemType.PLUGIN ? Integer.parseInt((String)item.get("rang_m1")) : rangM0;
                int rangM2 = typeItem != ItemType.COLOR && typeItem != ItemType.INVENTORY && typeItem != ItemType.PLUGIN ? Integer.parseInt((String)item.get("rang_m2")) : rangM0;
                int rangM3 = typeItem != ItemType.COLOR && typeItem != ItemType.INVENTORY && typeItem != ItemType.PLUGIN ? Integer.parseInt((String)item.get("rang_m3")) : rangM0;
                PropertyItem[] propertysItemM0 = null;
                PropertyItem[] propertysItemM1 = null;
                PropertyItem[] propertysItemM2 = null;
                PropertyItem[] propertysItemM3 = null;
                int countModification = typeItem == ItemType.COLOR ? 1 : (typeItem == ItemType.INVENTORY || typeItem == ItemType.PLUGIN ? (int)((Long)item.get("count_modifications")).longValue() : 4);
                block16: for (int m = 0; m < countModification; ++m) {
                    JSONArray propertys = (JSONArray)item.get("propertys_m" + m);
                    PropertyItem[] property = new PropertyItem[propertys.size()];
                    for (int p = 0; p < propertys.size(); ++p) {
                        JSONObject prop = (JSONObject)propertys.get(p);
                        property[p] = new PropertyItem(GarageItemsLoader.getType((String)prop.get("type")), (String)prop.get("value"));
                    }
                    switch (m) {
                        case 0: {
                            propertysItemM0 = property;
                            continue block16;
                        }
                        case 1: {
                            propertysItemM1 = property;
                            continue block16;
                        }
                        case 2: {
                            propertysItemM2 = property;
                            continue block16;
                        }
                        case 3: {
                            propertysItemM3 = property;
                        }
                    }
                }
                if (typeItem == ItemType.COLOR || typeItem == ItemType.INVENTORY || typeItem == ItemType.PLUGIN) {
                    propertysItemM1 = propertysItemM0;
                    propertysItemM2 = propertysItemM0;
                    propertysItemM3 = propertysItemM0;
                }
                switch (typeItem) {
                    case INVENTORY: {
                        priceM0 = GarageItemsLoader.applyDiscountToPrice(priceM0, 15);
                        priceM1 = GarageItemsLoader.applyDiscountToPrice(priceM1, 15);
                        priceM2 = GarageItemsLoader.applyDiscountToPrice(priceM2, 15);
                        priceM3 = GarageItemsLoader.applyDiscountToPrice(priceM3, 15);
                        if (!id.equals("gift")) break;
                        priceM0 = 500;
                        break;
                    }
                    case PLUGIN: {
                        break;
                    }
                    case WEAPON: {
                        priceM0 = GarageItemsLoader.applyDiscountToPrice(priceM0, 40);
                        priceM1 = GarageItemsLoader.applyDiscountToPrice(priceM1, 40);
                        priceM2 = GarageItemsLoader.applyDiscountToPrice(priceM2, 40);
                        priceM3 = GarageItemsLoader.applyDiscountToPrice(priceM3, 40);
                        break;
                    }
                    case COLOR: {
                        priceM0 = GarageItemsLoader.applyDiscountToPrice(priceM0, 40);
                        priceM1 = GarageItemsLoader.applyDiscountToPrice(priceM1, 40);
                        priceM2 = GarageItemsLoader.applyDiscountToPrice(priceM2, 40);
                        priceM3 = GarageItemsLoader.applyDiscountToPrice(priceM3, 40);
                        if (!id.equals("garland")) break;
                        priceM0 = 6000;
                        break;
                    }
                    case ARMOR: {
                        priceM0 = GarageItemsLoader.applyDiscountToPrice(priceM0, 40);
                        priceM1 = GarageItemsLoader.applyDiscountToPrice(priceM1, 40);
                        priceM2 = GarageItemsLoader.applyDiscountToPrice(priceM2, 40);
                        priceM3 = GarageItemsLoader.applyDiscountToPrice(priceM3, 40);
                    }
                }
                final ModificationInfo[] mods = new ModificationInfo[4];
                mods[0] = new ModificationInfo(id + "_m0", priceM0, rangM0);
                mods[0].propertys = propertysItemM0;
                mods[1] = new ModificationInfo(id + "_m1", priceM1, rangM1);
                mods[1].propertys = propertysItemM1;
                mods[2] = new ModificationInfo(id + "_m2", priceM2, rangM2);
                mods[2].propertys = propertysItemM2;
                mods[3] = new ModificationInfo(id + "_m3", priceM3, rangM3);
                mods[3].propertys = propertysItemM3;
                boolean specialItem = item.get("special_item") == null ? false : (Boolean)item.get("special_item");
                if (specialItem) {
                    HashSet<String> groups = new HashSet<String>();
                    Object groupsRaw = item.get("groups");
                    if (groupsRaw instanceof String) {
                        String group = ((String)groupsRaw).trim().toLowerCase(Locale.ROOT);
                        if (!group.isEmpty()) {
                            groups.add(group);
                        }
                    } else if (groupsRaw instanceof JSONArray) {
                        JSONArray groupsArray = (JSONArray)groupsRaw;
                        for (Object rawGroup : groupsArray) {
                            if (!(rawGroup instanceof String)) continue;
                            String group = ((String)rawGroup).trim().toLowerCase(Locale.ROOT);
                            if (group.isEmpty()) continue;
                            groups.add(group);
                        }
                    }
                    specialItemGroups.put(id, groups);
                }
                items.put(id, new Item(id, description, typeItem == ItemType.INVENTORY || typeItem == ItemType.PLUGIN, index, propertysItemM0, typeItem, 0, name, propertysItemM1, priceM1, rangM1, priceM0, rangM0, mods, specialItem, 0));
                ++index;
                if (typeItem != ItemType.COLOR) continue;
                ColormapsFactory.addColormap(id + "_m0", new Colormap(){
                    {
                        PropertyItem[] var5 = mods[0].propertys;
                        int var4 = mods[0].propertys.length;
                        for (int var3 = 0; var3 < var4; ++var3) {
                            PropertyItem _property = var5[var3];
                            this.addResistance(ColormapsFactory.getResistanceType(_property.property), GarageItemsLoader.getInt(_property.value.replace("%", "")));
                        }
                    }
                });
            }
        } catch (ParseException var29) {
            var29.printStackTrace();
        }
    }

    private static int applyDiscountToPrice(int originalPrice, int discountPercentage) {
        int discountedPrice = originalPrice - originalPrice * discountPercentage / 100;
        return discountedPrice;
    }

    private static int getInt(String str) {
        try {
            return Integer.parseInt(str);
        } catch (Exception var2) {
            return 0;
        }
    }

    private static PropertyType getType(String s) {
        for (PropertyType type : PropertyType.values()) {
            if (!type.toString().equals(s)) continue;
            return type;
        }
        return null;
    }


    public static boolean canGroupUseSpecialItem(String itemId, TypeUser groupType) {
        Item item = items.get(itemId);
        if (item == null || !item.specialItem) {
            return true;
        }
        HashSet<String> groups = specialItemGroups.get(itemId);
        if (groups == null || groups.isEmpty() || groupType == null) {
            return false;
        }
        return groups.contains(groupType.toString().toLowerCase(Locale.ROOT));
    }

    static {
        index = 1;
    }
}
