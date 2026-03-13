/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.battles.managers;

import gtanks.battles.maps.Map;
import gtanks.battles.tanks.math.Vector3;
import java.util.Random;

public class SpawnManager {
    private static final Random rand = new Random();

    public static Vector3 getSpawnState(Map map, String forTeam) {
        Vector3 pos;
        try {
            pos = forTeam.equals("BLUE") ? map.spawnPositonsBlue.get(rand.nextInt(map.spawnPositonsBlue.size())) : (forTeam.equals("RED") ? map.spawnPositonsRed.get(rand.nextInt(map.spawnPositonsRed.size())) : map.spawnPositonsDM.get(rand.nextInt(map.spawnPositonsDM.size())));
            if (pos == null) {
                pos = map.spawnPositonsDM.get(rand.nextInt(map.spawnPositonsDM.size()));
            }
        } catch (Exception var4) {
            pos = map.spawnPositonsDM.get(rand.nextInt(map.spawnPositonsDM.size()));
        }
        return pos;
    }
}

