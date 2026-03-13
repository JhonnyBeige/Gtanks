/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.battles.tanks.loaders;

import gtanks.battles.BattlefieldModel;
import gtanks.battles.BattlefieldPlayerController;
import gtanks.battles.tanks.weapons.IEntity;
import gtanks.battles.tanks.weapons.IWeapon;
import gtanks.battles.tanks.weapons.ShotData;
import gtanks.battles.tanks.weapons.WeaponWeakeningData;
import gtanks.battles.tanks.weapons.flamethrower.FlamethrowerEntity;
import gtanks.battles.tanks.weapons.flamethrower.FlamethrowerModel;
import gtanks.battles.tanks.weapons.frezee.FrezeeEntity;
import gtanks.battles.tanks.weapons.frezee.FrezeeModel;
import gtanks.battles.tanks.weapons.isida.IsidaEntity;
import gtanks.battles.tanks.weapons.isida.IsidaModel;
import gtanks.battles.tanks.weapons.isida.IsidaTESTModel;
import gtanks.battles.tanks.weapons.railgun.RailgunEntity;
import gtanks.battles.tanks.weapons.railgun.RailgunModel;
import gtanks.battles.tanks.weapons.ricochet.RicochetEntity;
import gtanks.battles.tanks.weapons.ricochet.RicochetModel;
import gtanks.battles.tanks.weapons.shaft.ShaftEntity;
import gtanks.battles.tanks.weapons.shaft.ShaftModel;
import gtanks.battles.tanks.weapons.smoky.SmokyEntity;
import gtanks.battles.tanks.weapons.smoky.SmokyModel;
import gtanks.battles.tanks.weapons.snowman.SnowmanEntity;
import gtanks.battles.tanks.weapons.snowman.SnowmanModel;
import gtanks.battles.tanks.weapons.thunder.ThunderEntity;
import gtanks.battles.tanks.weapons.thunder.ThunderHalloweenModel;
import gtanks.battles.tanks.weapons.thunder.ThunderModel;
import gtanks.battles.tanks.weapons.twins.TwinsEntity;
import gtanks.battles.tanks.weapons.twins.TwinsModel;
import gtanks.json.JSONUtils;
import gtanks.logger.Logger;
import gtanks.logger.Type;
import gtanks.main.ServerException;
import gtanks.utils.StringUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class WeaponsFactory {
    private static HashMap<String, IEntity> weapons = new HashMap();
    private static HashMap<String, WeaponWeakeningData> wwd = new HashMap();
    private static String jsonListWeapons;

    public static IWeapon getWeapon(String turretId, BattlefieldPlayerController tank, BattlefieldModel battle) {
        String turret;
        return switch (turret = turretId.split("_m")[0]) {
            case "smoky" -> new SmokyModel((SmokyEntity)WeaponsFactory.getEntity(turretId), WeaponsFactory.getWwd(turretId), battle, tank);
            case "smokyxt" -> new SmokyModel((SmokyEntity)WeaponsFactory.getEntity(turretId), WeaponsFactory.getWwd(turretId), battle, tank);
            case "flamethrower" -> new FlamethrowerModel((FlamethrowerEntity)WeaponsFactory.getEntity(turretId), battle, tank);
            case "flamethrowerhw" -> new FlamethrowerModel((FlamethrowerEntity)WeaponsFactory.getEntity(turretId), battle, tank);
            case "twins" -> new TwinsModel((TwinsEntity)WeaponsFactory.getEntity(turretId), WeaponsFactory.getWwd(turretId), tank, battle);
            case "twinsxt" -> new TwinsModel((TwinsEntity)WeaponsFactory.getEntity(turretId), WeaponsFactory.getWwd(turretId), tank, battle);
            case "railgun" -> new RailgunModel((RailgunEntity)WeaponsFactory.getEntity(turretId), tank, battle);
            case "railgunxt" -> new RailgunModel((RailgunEntity)WeaponsFactory.getEntity(turretId), tank, battle);
            case "isida" -> new IsidaModel((IsidaEntity)WeaponsFactory.getEntity(turretId), tank, battle);
            case "thunder" -> new ThunderModel((ThunderEntity)WeaponsFactory.getEntity(turretId), battle, tank);
            case "hwthunder" -> new ThunderHalloweenModel((ThunderEntity)WeaponsFactory.getEntity(turretId), battle, tank);
            case "frezee" -> new FrezeeModel((FrezeeEntity)WeaponsFactory.getEntity(turretId), battle, tank);
            case "frezeeny" -> new FrezeeModel((FrezeeEntity)WeaponsFactory.getEntity(turretId), battle, tank);
            case "ricochet" -> new RicochetModel((RicochetEntity)WeaponsFactory.getEntity(turretId), battle, tank);
            case "pumpkingun" -> new RicochetModel((RicochetEntity)WeaponsFactory.getEntity(turretId), battle, tank);
            case "snowman" -> new SnowmanModel((SnowmanEntity)WeaponsFactory.getEntity(turretId), WeaponsFactory.getWwd(turretId), tank, battle);
            case "shaft" -> new ShaftModel((ShaftEntity)WeaponsFactory.getEntity(turretId), WeaponsFactory.getWwd(turretId), battle, tank);
            case "isidahw" -> new IsidaTESTModel((IsidaEntity)WeaponsFactory.getEntity(turretId), tank, battle);
            case "bfg" -> new ThunderModel((ThunderEntity)WeaponsFactory.getEntity(turretId), battle, tank);
            default -> new RailgunModel((RailgunEntity)WeaponsFactory.getEntity("railgun_m0"), tank, battle);
        };
    }

    public static void init(String path2config) {
        weapons.clear();
        Logger.log("Weapons Factory inited. Loading weapons...");
        try {
            File folder = new File(path2config);
            for (File config : folder.listFiles()) {
                if (!config.getName().endsWith(".cfg")) {
                    throw new ServerException("In folder " + path2config + " find non-configuration file: " + config.getName());
                }
                Logger.log("Loading " + config.getName() + "...");
                WeaponsFactory.parse(config);
            }
            jsonListWeapons = JSONUtils.parseWeapons(WeaponsFactory.getEntitys(), wwd);
        } catch (Exception var7) {
            var7.printStackTrace();
            Logger.log(Type.ERROR, "Loading entitys weapons failed. " + var7.getMessage());
        }
    }

    private static void parse(File json) throws IOException, ParseException {
        JSONParser parser = new JSONParser();
        JSONObject jobj = (JSONObject)parser.parse(new InputStreamReader(new FileInputStream(json), StandardCharsets.UTF_8));
        String type = (String)jobj.get("type");
        for (Object item : (JSONArray)jobj.get("params")) {
            JSONObject jitem = (JSONObject)item;
            String modification = (String)jitem.get("modification");
            String id = StringUtils.concatStrings(type, "_", modification);
            ShotData shotData = new ShotData(id, WeaponsFactory.getDouble(jitem.get("autoAimingAngleDown")), WeaponsFactory.getDouble(jitem.get("autoAimingAngleUp")), (int)((Long)jitem.get("numRaysDown")).longValue(), (int)((Long)jitem.get("numRaysUp")).longValue(), (int)((Long)jitem.get("reloadMsec")).longValue(), (float)((Double)jitem.get("impactCoeff")).doubleValue(), (float)((Double)jitem.get("kickback")).doubleValue(), (float)((Double)jitem.get("turretRotationAccel")).doubleValue(), (float)((Double)jitem.get("turretRotationSpeed")).doubleValue());
            IEntity entity = null;
            switch (type) {
                case "smoky": {
                    WeaponWeakeningData wwd = new WeaponWeakeningData((Double)jitem.get("max_damage_radius"), (Double)jitem.get("min_damage_percent"), (Double)jitem.get("min_damage_radius"));
                    entity = new SmokyEntity(shotData, (float)((Double)jitem.get("min_damage")).doubleValue(), (float)((Double)jitem.get("max_damage")).doubleValue());
                    WeaponsFactory.wwd.put(id, wwd);
                    break;
                }
                case "smokyxt": {
                    WeaponWeakeningData wwd = new WeaponWeakeningData((Double)jitem.get("max_damage_radius"), (Double)jitem.get("min_damage_percent"), (Double)jitem.get("min_damage_radius"));
                    entity = new SmokyEntity(shotData, (float)((Double)jitem.get("min_damage")).doubleValue(), (float)((Double)jitem.get("max_damage")).doubleValue());
                    WeaponsFactory.wwd.put(id, wwd);
                    break;
                }
                case "flamethrower": {
                    entity = new FlamethrowerEntity((int)((Long)jitem.get("target_detection_interval")).longValue(), (float)((Double)jitem.get("range")).doubleValue(), (float)((Double)jitem.get("cone_angle")).doubleValue(), (int)((Long)jitem.get("heating_speed")).longValue(), (int)((Long)jitem.get("cooling_speed")).longValue(), (int)((Long)jitem.get("heat_limit")).longValue(), shotData, (float)((Double)jitem.get("max_damage")).doubleValue(), (float)((Double)jitem.get("min_damage")).doubleValue());
                    break;
                }
                case "flamethrowerhw": {
                    entity = new FlamethrowerEntity((int)((Long)jitem.get("target_detection_interval")).longValue(), (float)((Double)jitem.get("range")).doubleValue(), (float)((Double)jitem.get("cone_angle")).doubleValue(), (int)((Long)jitem.get("heating_speed")).longValue(), (int)((Long)jitem.get("cooling_speed")).longValue(), (int)((Long)jitem.get("heat_limit")).longValue(), shotData, (float)((Double)jitem.get("max_damage")).doubleValue(), (float)((Double)jitem.get("min_damage")).doubleValue());
                    break;
                }
                case "twins": {
                    WeaponWeakeningData wwdTwins = new WeaponWeakeningData((Double)jitem.get("max_damage_radius"), (Double)jitem.get("min_damage_percent"), (Double)jitem.get("min_damage_radius"));
                    entity = new TwinsEntity((float)((Double)jitem.get("shot_range")).doubleValue(), (float)((Double)jitem.get("shot_speed")).doubleValue(), (float)((Double)jitem.get("shot_radius")).doubleValue(), (float)((Double)jitem.get("min_damage")).doubleValue(), (float)((Double)jitem.get("max_damage")).doubleValue(), shotData);
                    wwd.put(id, wwdTwins);
                    break;
                }
                case "twinsxt": {
                    WeaponWeakeningData wwdTwins = new WeaponWeakeningData((Double)jitem.get("max_damage_radius"), (Double)jitem.get("min_damage_percent"), (Double)jitem.get("min_damage_radius"));
                    entity = new TwinsEntity((float)((Double)jitem.get("shot_range")).doubleValue(), (float)((Double)jitem.get("shot_speed")).doubleValue(), (float)((Double)jitem.get("shot_radius")).doubleValue(), (float)((Double)jitem.get("min_damage")).doubleValue(), (float)((Double)jitem.get("max_damage")).doubleValue(), shotData);
                    wwd.put(id, wwdTwins);
                    break;
                }
                case "railgun": {
                    entity = new RailgunEntity(shotData, (int)((Long)jitem.get("charingTime")).longValue(), (int)((Long)jitem.get("weakeningCoeff")).longValue(), (float)((Double)jitem.get("min_damage")).doubleValue(), (float)((Double)jitem.get("max_damage")).doubleValue());
                    break;
                }
                case "railgunxt": {
                    entity = new RailgunEntity(shotData, (int)((Long)jitem.get("charingTime")).longValue(), (int)((Long)jitem.get("weakeningCoeff")).longValue(), (float)((Double)jitem.get("min_damage")).doubleValue(), (float)((Double)jitem.get("max_damage")).doubleValue());
                    break;
                }
                case "isida": {
                    entity = new IsidaEntity((int)((Long)jitem.get("capacity")).longValue(), (int)((Long)jitem.get("chargeRate")).longValue(), (int)((Long)jitem.get("dischargeRate")).longValue(), (int)((Long)jitem.get("tickPeriod")).longValue(), (float)((Double)jitem.get("lockAngle")).doubleValue(), (float)((Double)jitem.get("lockAngleCos")).doubleValue(), (float)((Double)jitem.get("maxAngle")).doubleValue(), (float)((Double)jitem.get("maxAngleCos")).doubleValue(), (float)((Double)jitem.get("maxRadius")).doubleValue(), shotData, (float)((Double)jitem.get("min_damage")).doubleValue(), (float)((Double)jitem.get("max_damage")).doubleValue());
                    break;
                }
                case "isidahw": {
                    entity = new IsidaEntity((int)((Long)jitem.get("capacity")).longValue(), (int)((Long)jitem.get("chargeRate")).longValue(), (int)((Long)jitem.get("dischargeRate")).longValue(), (int)((Long)jitem.get("tickPeriod")).longValue(), (float)((Double)jitem.get("lockAngle")).doubleValue(), (float)((Double)jitem.get("lockAngleCos")).doubleValue(), (float)((Double)jitem.get("maxAngle")).doubleValue(), (float)((Double)jitem.get("maxAngleCos")).doubleValue(), (float)((Double)jitem.get("maxRadius")).doubleValue(), shotData, (float)((Double)jitem.get("min_damage")).doubleValue(), (float)((Double)jitem.get("max_damage")).doubleValue());
                    break;
                }
                case "thunder": {
                    WeaponWeakeningData wwdThunder = new WeaponWeakeningData((Double)jitem.get("maxSplashDamageRadius"), (Double)jitem.get("minSplashDamageRadius"), (Double)jitem.get("minSplashDamagePercent"));
                    entity = new ThunderEntity((float)((Double)jitem.get("maxSplashDamageRadius")).doubleValue(), (float)((Double)jitem.get("minSplashDamageRadius")).doubleValue(), (float)((Double)jitem.get("minSplashDamagePercent")).doubleValue(), (float)((Double)jitem.get("impactForce")).doubleValue(), shotData, (float)((Double)jitem.get("min_damage")).doubleValue(), (float)((Double)jitem.get("max_damage")).doubleValue(), wwdThunder);
                    wwd.put(id, wwdThunder);
                    break;
                }
                case "frezee": {
                    entity = new FrezeeEntity((float)((Double)jitem.get("damageAreaConeAngle")).doubleValue(), (float)((Double)jitem.get("damageAreaRange")).doubleValue(), (int)((Long)jitem.get("energyCapacity")).longValue(), (int)((Long)jitem.get("energyDischargeSpeed")).longValue(), (int)((Long)jitem.get("energyRechargeSpeed")).longValue(), (int)((Long)jitem.get("weaponTickMsec")).longValue(), (float)((Double)jitem.get("coolingSpeed")).doubleValue(), (float)((Double)jitem.get("min_damage")).doubleValue(), (float)((Double)jitem.get("max_damage")).doubleValue(), shotData);
                    break;
                }
                case "frezeeny": {
                    entity = new FrezeeEntity((float)((Double)jitem.get("damageAreaConeAngle")).doubleValue(), (float)((Double)jitem.get("damageAreaRange")).doubleValue(), (int)((Long)jitem.get("energyCapacity")).longValue(), (int)((Long)jitem.get("energyDischargeSpeed")).longValue(), (int)((Long)jitem.get("energyRechargeSpeed")).longValue(), (int)((Long)jitem.get("weaponTickMsec")).longValue(), (float)((Double)jitem.get("coolingSpeed")).doubleValue(), (float)((Double)jitem.get("min_damage")).doubleValue(), (float)((Double)jitem.get("max_damage")).doubleValue(), shotData);
                    break;
                }
                case "ricochet": {
                    WeaponWeakeningData wwdRicochet = new WeaponWeakeningData((Double)jitem.get("max_damage_radius"), (Double)jitem.get("min_damage_percent"), (Double)jitem.get("min_damage_radius"));
                    entity = new RicochetEntity((float)((Double)jitem.get("shotRadius")).doubleValue(), (float)((Double)jitem.get("shotSpeed")).doubleValue(), (int)((Long)jitem.get("energyCapacity")).longValue(), (int)((Long)jitem.get("energyPerShot")).longValue(), (float)((Double)jitem.get("energyRechargeSpeed")).doubleValue(), (float)((Double)jitem.get("shotDistance")).doubleValue(), (float)((Double)jitem.get("min_damage")).doubleValue(), (float)((Double)jitem.get("max_damage")).doubleValue(), shotData);
                    wwd.put(id, wwdRicochet);
                    break;
                }
                case "pumpkingun": {
                    WeaponWeakeningData wwdRicochet = new WeaponWeakeningData((Double)jitem.get("max_damage_radius"), (Double)jitem.get("min_damage_percent"), (Double)jitem.get("min_damage_radius"));
                    entity = new RicochetEntity((float)((Double)jitem.get("shotRadius")).doubleValue(), (float)((Double)jitem.get("shotSpeed")).doubleValue(), (int)((Long)jitem.get("energyCapacity")).longValue(), (int)((Long)jitem.get("energyPerShot")).longValue(), (float)((Double)jitem.get("energyRechargeSpeed")).doubleValue(), (float)((Double)jitem.get("shotDistance")).doubleValue(), (float)((Double)jitem.get("min_damage")).doubleValue(), (float)((Double)jitem.get("max_damage")).doubleValue(), shotData);
                    wwd.put(id, wwdRicochet);
                    break;
                }
                case "snowman": {
                    WeaponWeakeningData wwdSnowman = new WeaponWeakeningData((Double)jitem.get("max_damage_radius"), (Double)jitem.get("min_damage_percent"), (Double)jitem.get("min_damage_radius"));
                    entity = new SnowmanEntity((float)((Double)jitem.get("shot_range")).doubleValue(), (float)((Double)jitem.get("shot_speed")).doubleValue(), (float)((Double)jitem.get("shot_radius")).doubleValue(), (float)((Double)jitem.get("min_damage")).doubleValue(), (float)((Double)jitem.get("max_damage")).doubleValue(), (float)((Double)jitem.get("frezee_speed")).doubleValue(), shotData);
                    wwd.put(id, wwdSnowman);
                    break;
                }
                case "bfg": {
                    WeaponWeakeningData wwdThunder = new WeaponWeakeningData((Double)jitem.get("maxSplashDamageRadius"), (Double)jitem.get("minSplashDamageRadius"), (Double)jitem.get("minSplashDamagePercent"));
                    entity = new ThunderEntity((float)((Double)jitem.get("maxSplashDamageRadius")).doubleValue(), (float)((Double)jitem.get("minSplashDamageRadius")).doubleValue(), (float)((Double)jitem.get("minSplashDamagePercent")).doubleValue(), (float)((Double)jitem.get("impactForce")).doubleValue(), shotData, (float)((Double)jitem.get("min_damage")).doubleValue(), (float)((Double)jitem.get("max_damage")).doubleValue(), wwdThunder);
                    wwd.put(id, wwdThunder);
                    break;
                }
                case "hwthunder": {
                    WeaponWeakeningData wwdThunder = new WeaponWeakeningData((Double)jitem.get("maxSplashDamageRadius"), (Double)jitem.get("minSplashDamageRadius"), (Double)jitem.get("minSplashDamagePercent"));
                    entity = new ThunderEntity((float)((Double)jitem.get("maxSplashDamageRadius")).doubleValue(), (float)((Double)jitem.get("minSplashDamageRadius")).doubleValue(), (float)((Double)jitem.get("minSplashDamagePercent")).doubleValue(), (float)((Double)jitem.get("impactForce")).doubleValue(), shotData, (float)((Double)jitem.get("min_damage")).doubleValue(), (float)((Double)jitem.get("max_damage")).doubleValue(), wwdThunder);
                    wwd.put(id, wwdThunder);
                    break;
                }
                case "shaft": {
                    WeaponWeakeningData shaftwwd = new WeaponWeakeningData((Double)jitem.get("max_damage_radius"), (Double)jitem.get("min_damage_percent"), (Double)jitem.get("min_damage_radius"));
                    entity = new ShaftEntity((float)((Double)jitem.get("min_damage")).doubleValue(), (float)((Double)jitem.get("max_damage")).doubleValue(), (float)((Double)jitem.get("fov_max_damage")).doubleValue(), (float)((Double)jitem.get("max_energy")).doubleValue(), (float)((Double)jitem.get("charge_rate")).doubleValue(), (float)((Double)jitem.get("discharge_rate")).doubleValue(), (float)((Double)jitem.get("elevation_angle_up")).doubleValue(), (float)((Double)jitem.get("elevation_angle_down")).doubleValue(), (float)((Double)jitem.get("vertical_targeting_speed")).doubleValue(), (float)((Double)jitem.get("horizontal_targeting_speed")).doubleValue(), (float)((Double)jitem.get("inital_fov")).doubleValue(), (float)((Double)jitem.get("minimum_fov")).doubleValue(), (float)((Double)jitem.get("shrubs_hiding_radius_min")).doubleValue(), (float)((Double)jitem.get("shrubs_hiding_radius_max")).doubleValue(), (float)((Double)jitem.get("impact_quick_shot")).doubleValue(), shotData);
                    wwd.put(id, shaftwwd);
                }
            }
            weapons.put(id, entity);
        }
    }

    public static WeaponWeakeningData getWwd(String id) {
        return wwd.get(id);
    }

    public static IEntity getEntity(String id) {
        return weapons.get(id);
    }

    public static String getId(IEntity entity) {
        String id = null;
        for (Map.Entry<String, IEntity> entry : weapons.entrySet()) {
            if (!entry.getValue().equals(entity)) continue;
            id = entry.getKey();
        }
        return id;
    }

    private static double getDouble(Object obj) {
        try {
            return (Double)obj;
        } catch (Exception var2) {
            return ((Long)obj).longValue();
        }
    }

    public static Collection<IEntity> getEntitys() {
        return weapons.values();
    }

    public static String getJSONList() {
        return jsonListWeapons;
    }
}

