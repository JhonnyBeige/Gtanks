/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.battles.maps;

import gtanks.battles.maps.IMapConfigItem;
import gtanks.battles.maps.Map;
import gtanks.battles.maps.parser.Parser;
import gtanks.battles.maps.parser.map.bonus.BonusRegion;
import gtanks.battles.maps.parser.map.bonus.BonusType;
import gtanks.battles.maps.parser.map.spawn.SpawnPosition;
import gtanks.battles.maps.parser.map.spawn.SpawnPositionType;
import gtanks.battles.maps.themes.MapThemeFactory;
import gtanks.battles.tanks.math.Vector3;
import gtanks.logger.Logger;
import gtanks.utils.ResourceUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import javax.xml.bind.JAXBException;
import org.apache.commons.codec.digest.DigestUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class MapsLoader {
    public static HashMap<String, Map> maps = new HashMap();
    private static ArrayList<IMapConfigItem> configItems = new ArrayList();
    private static Parser parser;

    public static void initFactoryMaps() {
        Logger.log("Maps Loader Factory inited. Loading maps...");
        try {
            parser = new Parser();
        } catch (JAXBException var1) {
            var1.printStackTrace();
        }
        MapsLoader.loadConfig();
    }

    private static void loadConfig() {
        try {
            JSONParser mapsParser = new JSONParser();
            Object items = mapsParser.parse(new InputStreamReader(new FileInputStream(ResourceUtils.data("json/mapsConfig.json")), StandardCharsets.UTF_8));
            JSONObject obj = (JSONObject)items;
            JSONArray jarray = (JSONArray)obj.get("maps");
            for (Object objItem : jarray) {
                IMapConfigItem __item;
                JSONObject item = (JSONObject)objItem;
                String id = (String)item.get("id");
                String name = (String)item.get("name");
                String skyboxId = (String)item.get("skybox_id");
                Object ambientSoundId = item.get("ambient_sound_id");
                Object gameModeId = item.get("gamemode_id");
                int minRank = Integer.parseInt((String)item.get("min_rank"));
                int maxRank = Integer.parseInt((String)item.get("max_rank"));
                int maxPlayers = Integer.parseInt((String)item.get("max_players"));
                boolean tdm = (Boolean)item.get("tdm");
                boolean ctf = (Boolean)item.get("ctf");
                Object themeId = item.get("theme_id");
                IMapConfigItem iMapConfigItem = __item = ambientSoundId != null && gameModeId != null ? new IMapConfigItem(id, name, skyboxId, minRank, maxRank, maxPlayers, tdm, ctf, (String)ambientSoundId, (String)gameModeId) : new IMapConfigItem(id, name, skyboxId, minRank, maxRank, maxPlayers, tdm, ctf);
                if (themeId != null) {
                    __item.themeName = (String)themeId;
                }
                configItems.add(__item);
            }
            MapsLoader.parseMaps();
        } catch (IOException | ParseException var19) {
            var19.printStackTrace();
        }
    }

    private static void parseMaps() {
        File[] maps;
        File[] var4 = maps = new File(ResourceUtils.data("maps")).listFiles();
        int var3 = maps.length;
        for (int var2 = 0; var2 < var3; ++var2) {
            File file = var4[var2];
            if (file.isDirectory() || !file.getName().endsWith(".xml")) continue;
            MapsLoader.parse(file);
        }
        Logger.log("Loaded all maps!\n");
    }

    private static void parse(final File file) {
        Logger.log("Loading " + file.getName() + "...");
        final IMapConfigItem temp = MapsLoader.getMapItem(file.getName().substring(0, file.getName().length() - 4));
        if (temp != null) {
            Map map = null;
            try {
                map = new Map(){
                    {
                        this.name = temp.name;
                        this.id = temp.id;
                        this.skyboxId = temp.skyboxId;
                        this.minRank = temp.minRank;
                        this.maxRank = temp.maxRank;
                        this.maxPlayers = temp.maxPlayers;
                        this.tdm = temp.tdm;
                        this.ctf = temp.ctf;
                        this.md5Hash = DigestUtils.md5Hex(new FileInputStream(file));
                        this.mapTheme = temp.ambientSoundId != null && temp.gameMode != null ? MapThemeFactory.getMapTheme(temp.ambientSoundId, temp.gameMode) : MapThemeFactory.getDefaultMapTheme();
                        this.themeId = temp.themeName;
                    }
                };
            } catch (IOException var9) {
                var9.printStackTrace();
            }
            gtanks.battles.maps.parser.map.Map parsedMap = null;
            try {
                parsedMap = parser.parseMap(file);
            } catch (JAXBException var8) {
                var8.printStackTrace();
            }
            for (SpawnPosition sp : parsedMap.getSpawnPositions()) {
                if (sp.getSpawnPositionType() == SpawnPositionType.NONE) {
                    map.spawnPositonsDM.add(sp.getVector3());
                }
                if (sp.getSpawnPositionType() == SpawnPositionType.RED) {
                    map.spawnPositonsRed.add(sp.getVector3());
                }
                if (sp.getSpawnPositionType() != SpawnPositionType.BLUE) continue;
                map.spawnPositonsBlue.add(sp.getVector3());
            }
            if (parsedMap.getBonusesRegion() != null) {
                for (BonusRegion br : parsedMap.getBonusesRegion()) {
                    for (BonusType type : br.getType()) {
                        if (type == BonusType.CRYSTALL) {
                            map.crystallsRegions.add(br.toServerBonusRegion());
                            continue;
                        }
                        if (type == BonusType.CRYSTALL_100) {
                            map.goldsRegions.add(br.toServerBonusRegion());
                            continue;
                        }
                        if (type == BonusType.CRYSTALL) {
                            map.crystallsRegions.add(br.toServerBonusRegion());
                            continue;
                        }
                        if (type == BonusType.ARMOR) {
                            map.armorsRegions.add(br.toServerBonusRegion());
                            continue;
                        }
                        if (type == BonusType.DAMAGE) {
                            map.damagesRegions.add(br.toServerBonusRegion());
                            continue;
                        }
                        if (type == BonusType.HEAL) {
                            map.healthsRegions.add(br.toServerBonusRegion());
                            continue;
                        }
                        if (type != BonusType.NITRO) continue;
                        map.nitrosRegions.add(br.toServerBonusRegion());
                    }
                }
            }
            map.flagBluePosition = parsedMap.getPositionBlueFlag() != null ? parsedMap.getPositionBlueFlag().toVector3() : null;
            Vector3 vector3 = map.flagRedPosition = parsedMap.getPositionRedFlag() != null ? parsedMap.getPositionRedFlag().toVector3() : null;
            if (map.flagBluePosition != null) {
                Vector3 var10000 = map.flagBluePosition;
                var10000.z += 50.0f;
                var10000 = map.flagRedPosition;
                var10000.z += 50.0f;
            }
            if (parsedMap.getPoints() != null) {
                map.domKeypoints = parsedMap.getDOMKeypoints();
            }
            maps.put(map.id, map);
        }
    }

    private static IMapConfigItem getMapItem(String id) {
        IMapConfigItem item;
        Iterator<IMapConfigItem> var2 = configItems.iterator();
        do {
            if (!var2.hasNext()) {
                return null;
            }
            item = var2.next();
        } while (!item.id.equals(id));
        return item;
    }
}

