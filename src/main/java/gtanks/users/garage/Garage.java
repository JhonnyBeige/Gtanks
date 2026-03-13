/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.users.garage;

import gtanks.users.TypeUser;
import gtanks.users.garage.GarageItemsLoader;
import gtanks.users.garage.enums.ItemType;
import gtanks.users.garage.items.Item;
import java.io.Serializable;
import java.util.ArrayList;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

@Entity
@org.hibernate.annotations.Entity
@Table(name="garages")
public class Garage
implements Serializable {
    private static final long serialVersionUID = 2342422342L;
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="uid", nullable=false, unique=true)
    private long id;
    @Column(name="turrets", nullable=false)
    private String _json_turrets;
    @Column(name="hulls", nullable=false)
    private String _json_hulls;
    @Column(name="colormaps", nullable=false)
    private String _json_colormaps;
    @Column(name="inventory", nullable=false)
    private String _json_inventory;
    @Column(name="effects", nullable=false)
    private String _json_effects;
    @Column(name="userid", nullable=false, unique=true)
    private String userId;
    @Transient
    public ArrayList<Item> items = new ArrayList();
    @Transient
    public Item mountTurret;
    @Transient
    public Item mountHull;
    @Transient
    public Item mountColormap;

    public Garage() {
        this.items.add(GarageItemsLoader.items.get("smoky").clone());
        this.items.add(GarageItemsLoader.items.get("wasp").clone());
        this.items.add(GarageItemsLoader.items.get("green").clone());
        this.items.add(GarageItemsLoader.items.get("holiday").clone());
        this.items.add(GarageItemsLoader.items.get("up_score_start").clone());
        this.mountItem("wasp_m0");
        this.mountItem("smoky_m0");
        this.mountItem("green_m0");
    }

    public boolean hasActiveEffect(String id) {
        Item item = this.getItemById(id);
        return item != null && item.itemType == ItemType.PLUGIN && item.timeRemaining > (double)System.currentTimeMillis();
    }

    public Item grantTimedEffect(String effectId, int days) {
        Item base = GarageItemsLoader.items.get(effectId);
        if (base == null || base.itemType != ItemType.PLUGIN) {
            return null;
        }
        Item fromUser = this.getItemById(effectId);
        if (fromUser == null) {
            fromUser = base.clone();
            this.items.add(fromUser);
        }
        fromUser.setRemainingDurationDays(days);
        return fromUser;
    }

    public boolean syncSpecialItemsForGroup(TypeUser groupType) {
        boolean changed = false;
        for (Item template : GarageItemsLoader.items.values()) {
            if (!template.specialItem) continue;
            boolean allowed = GarageItemsLoader.canGroupUseSpecialItem(template.id, groupType);
            Item owned = this.getItemById(template.id);
            if (!allowed && owned != null) {
                this.items.remove(owned);
                if (this.mountTurret == owned) {
                    this.mountTurret = null;
                }
                if (this.mountHull == owned) {
                    this.mountHull = null;
                }
                if (this.mountColormap == owned) {
                    this.mountColormap = null;
                }
                changed = true;
            }
        }
        if (this.mountTurret == null) {
            Item defaultTurret = this.getItemById("smoky");
            if (defaultTurret != null) {
                this.mountTurret = defaultTurret;
            }
        }
        if (this.mountHull == null) {
            Item defaultHull = this.getItemById("wasp");
            if (defaultHull != null) {
                this.mountHull = defaultHull;
            }
        }
        if (this.mountColormap == null) {
            Item defaultColormap = this.getItemById("green");
            if (defaultColormap != null) {
                this.mountColormap = defaultColormap;
            }
        }
        return changed;
    }

    public boolean containsItem(String id) {
        for (Item item : this.items) {
            if (!item.id.equals(id)) continue;
            return true;
        }
        return false;
    }

    public Item getItemById(String id) {
        for (Item item : this.items) {
            if (!item.id.equals(id)) continue;
            return item;
        }
        return null;
    }

    public boolean mountItem(String id) {
        Item item = this.getItemById(id.substring(0, id.length() - 3));
        if (item != null && Integer.parseInt(id.substring(id.length() - 1, id.length())) == item.modificationIndex) {
            if (item.itemType == ItemType.WEAPON) {
                this.mountTurret = item;
                return true;
            }
            if (item.itemType == ItemType.ARMOR) {
                this.mountHull = item;
                return true;
            }
            if (item.itemType == ItemType.COLOR) {
                this.mountColormap = item;
                return true;
            }
        }
        return false;
    }

    public boolean updateItem(String id) {
        Item item = this.getItemById(id.substring(0, id.length() - 3));
        int modificationID = Integer.parseInt(id.substring(id.length() - 1));
        if (modificationID < 3 && item.modificationIndex == modificationID) {
            ++item.modificationIndex;
            item.nextPrice = item.modifications[item.modificationIndex + 1 != 4 ? item.modificationIndex + 1 : item.modificationIndex].price;
            item.nextProperty = item.modifications[item.modificationIndex + 1 != 4 ? item.modificationIndex + 1 : item.modificationIndex].propertys;
            item.nextRankId = item.modifications[item.modificationIndex + 1 != 4 ? item.modificationIndex + 1 : item.modificationIndex].rank;
            this.replaceItems(this.getItemById(id.substring(0, id.length() - 3)), item);
            return true;
        }
        return false;
    }

    public Item buyItem(String id, int count, TypeUser groupType) {
        id = id.substring(0, id.length() - 3);
        Item temp = GarageItemsLoader.items.get(id);
        if (temp.specialItem && !GarageItemsLoader.canGroupUseSpecialItem(temp.id, groupType)) {
            return null;
        }
        Item item = temp.clone();
        if (item.id.equals("1000_scores")) {
            return item;
        }
        if (!this.items.contains(this.getItemById(id))) {
            if (item.itemType == ItemType.INVENTORY) {
                item.count += count;
            }
            this.items.add(item);
            return item;
        }
        if (!this.items.contains(this.getItemById(id))) {
            if (item.itemType == ItemType.PLUGIN) {
                item.count += count;
            }
            this.items.add(item);
            return item;
        }
        if (item.itemType == ItemType.INVENTORY) {
            Item fromUser = this.getItemById(id);
            fromUser.count += count;
            return fromUser;
        }
        if (item.itemType == ItemType.PLUGIN) {
            Item fromUser = this.getItemById(id);
            fromUser.count += count;
            return fromUser;
        }
        return null;
    }

    private void replaceItems(Item old, Item newItem) {
        if (this.items.contains(old)) {
            this.items.set(this.items.indexOf(old), newItem);
        }
    }

    public ArrayList<Item> getInventoryItems() {
        ArrayList<Item> _items = new ArrayList<Item>();
        for (Item item : this.items) {
            if (item.itemType != ItemType.INVENTORY) continue;
            _items.add(item);
        }
        return _items;
    }

    public ArrayList<Item> getEffectItems() {
        ArrayList<Item> _items = new ArrayList<Item>();
        for (Item item : this.items) {
            if (item.itemType != ItemType.PLUGIN) continue;
            _items.add(item);
        }
        return _items;
    }

    public void parseJSONData() {
        JSONObject hulls = new JSONObject();
        JSONArray _hulls = new JSONArray();
        JSONObject colormaps = new JSONObject();
        JSONArray _colormaps = new JSONArray();
        JSONObject turrets = new JSONObject();
        JSONArray _turrets = new JSONArray();
        JSONObject inventory_items = new JSONObject();
        JSONArray _inventory = new JSONArray();
        JSONObject effects_items = new JSONObject();
        JSONArray _effects = new JSONArray();
        for (Item item : this.items) {
            if (item.itemType == ItemType.ARMOR) {
                JSONObject hull = new JSONObject();
                hull.put("id", item.id);
                hull.put("modification", item.modificationIndex);
                hull.put("mounted", item == this.mountHull);
                _hulls.add(hull);
            }
            if (item.itemType == ItemType.COLOR) {
                JSONObject colormap = new JSONObject();
                colormap.put("id", item.id);
                colormap.put("modification", item.modificationIndex);
                colormap.put("mounted", item == this.mountColormap);
                _colormaps.add(colormap);
            }
            if (item.itemType == ItemType.WEAPON) {
                JSONObject turret = new JSONObject();
                turret.put("id", item.id);
                turret.put("modification", item.modificationIndex);
                turret.put("mounted", item == this.mountTurret);
                _turrets.add(turret);
            }
            if (item.itemType == ItemType.INVENTORY) {
                JSONObject inventory = new JSONObject();
                inventory.put("id", item.id);
                inventory.put("count", item.count);
                _inventory.add(inventory);
            }
            if (item.itemType != ItemType.PLUGIN) continue;
            JSONObject effects = new JSONObject();
            effects.put("id", item.id);
            effects.put("time", item.timeRemaining);
            _effects.add(effects);
        }
        hulls.put("hulls", _hulls);
        colormaps.put("colormaps", _colormaps);
        turrets.put("turrets", _turrets);
        inventory_items.put("inventory", _inventory);
        effects_items.put("effects", _effects);
        this._json_colormaps = colormaps.toJSONString();
        this._json_hulls = hulls.toJSONString();
        this._json_turrets = turrets.toJSONString();
        this._json_inventory = inventory_items.toJSONString();
        this._json_effects = effects_items.toJSONString();
    }

    public void unparseJSONData() throws ParseException {
        Item item;
        this.items.clear();
        JSONParser parser = new JSONParser();
        JSONObject turrets = (JSONObject)parser.parse(this._json_turrets);
        JSONObject colormaps = (JSONObject)parser.parse(this._json_colormaps);
        JSONObject hulls = (JSONObject)parser.parse(this._json_hulls);
        JSONObject inventory = this._json_inventory == null || this._json_inventory.isEmpty() ? null : (JSONObject)parser.parse(this._json_inventory);
        JSONObject effects = this._json_effects == null || this._json_effects.isEmpty() ? null : (JSONObject)parser.parse(this._json_effects);
        for (Object _turret : (JSONArray)turrets.get("turrets")) {
            JSONObject turret = (JSONObject)_turret;
            item = GarageItemsLoader.items.get(turret.get("id")).clone();
            item.modificationIndex = (int)((Long)turret.get("modification")).longValue();
            item.nextRankId = item.modifications[item.modificationIndex == 3 ? 3 : item.modificationIndex + 1].rank;
            item.nextPrice = item.modifications[item.modificationIndex == 3 ? 3 : item.modificationIndex + 1].price;
            this.items.add(item);
            if (!((Boolean)turret.get("mounted")).booleanValue()) continue;
            this.mountTurret = item;
        }
        for (Object _colormap : (JSONArray)colormaps.get("colormaps")) {
            JSONObject colormap = (JSONObject)_colormap;
            item = GarageItemsLoader.items.get(colormap.get("id")).clone();
            item.modificationIndex = (int)((Long)colormap.get("modification")).longValue();
            this.items.add(item);
            if (!((Boolean)colormap.get("mounted")).booleanValue()) continue;
            this.mountColormap = item;
        }
        for (Object _hull : (JSONArray)hulls.get("hulls")) {
            JSONObject hull = (JSONObject)_hull;
            item = GarageItemsLoader.items.get(hull.get("id")).clone();
            item.modificationIndex = (int)((Long)hull.get("modification")).longValue();
            item.nextRankId = item.modifications[item.modificationIndex == 3 ? 3 : item.modificationIndex + 1].rank;
            item.nextPrice = item.modifications[item.modificationIndex == 3 ? 3 : item.modificationIndex + 1].price;
            this.items.add(item);
            if (!((Boolean)hull.get("mounted")).booleanValue()) continue;
            this.mountHull = item;
        }
        if (inventory != null) {
            JSONObject _item;
            for (Object inventory_item : (JSONArray)inventory.get("inventory")) {
                _item = (JSONObject)inventory_item;
                item = GarageItemsLoader.items.get(_item.get("id")).clone();
                item.modificationIndex = 0;
                item.count = (int)((Long)_item.get("count")).longValue();
                if (item.itemType != ItemType.INVENTORY) continue;
                this.items.add(item);
            }
            if (effects != null) {
                for (Object effects_item : (JSONArray)effects.get("effects")) {
                    _item = (JSONObject)effects_item;
                    item = GarageItemsLoader.items.get(_item.get("id")).clone();
                    item.modificationIndex = 0;
                    item.timeRemaining = (Double)_item.get("time");
                    if (item.itemType != ItemType.PLUGIN) continue;
                    this.items.add(item);
                }
            }
        }
    }

    public String getUserId() {
        return this.userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
