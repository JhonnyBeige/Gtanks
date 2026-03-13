/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.battles.maps.parser.map;

import gtanks.battles.maps.parser.map.spawn.SpawnPosition;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="spawn-points")
class SpawnPoints {
    private List<SpawnPosition> spawnPositions;

    SpawnPoints() {
    }

    public List<SpawnPosition> getSpawnPositions() {
        return this.spawnPositions;
    }

    @XmlElement(name="spawn-point")
    public void setSpawnPositions(List<SpawnPosition> spawnPositions) {
        this.spawnPositions = spawnPositions;
    }
}

