/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.battles.tanks.loaders;

import gtanks.battles.tanks.hulls.Hull;
import gtanks.main.ServerException;
import gtanks.utils.StringUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.util.HashMap;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class HullsFactory {
    private static final HashMap<String, Hull> hulls = new HashMap();

    public static void init(String path2configs) {
        hulls.clear();
        try {
            File file = new File(path2configs);
            for (File config : file.listFiles()) {
                HullsFactory.parse(config);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void parse(File config) throws IOException, ParseException {
        JSONObject jobj = (JSONObject)new JSONParser().parse(new InputStreamReader(new FileInputStream(config), StandardCharsets.UTF_8));
        String type = (String)jobj.get("type");
        for (Object obj : (JSONArray)jobj.get("modifications")) {
            JSONObject jt = (JSONObject)obj;
            Hull hull = new Hull((float)((Double)jt.get("mass")).doubleValue(), (float)((Double)jt.get("power")).doubleValue(), (float)((Double)jt.get("speed")).doubleValue(), (float)((Double)jt.get("turn_speed")).doubleValue(), ((Long)jt.get("hp")).longValue());
            hulls.put(StringUtils.concatStrings(type, "_", (String)jt.get("modification")), hull);
        }
    }

    public static Hull getHull(String id) {
        Hull hull = hulls.get(id);
        if (hull == null) {
            new ServerException("Hull with id " + id + " is null!");
            return null;
        }
        return hull;
    }
}

