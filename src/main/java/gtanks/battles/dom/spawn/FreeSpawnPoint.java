/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.battles.dom.spawn;

import gtanks.battles.tanks.math.Vector3;

public class FreeSpawnPoint {
    private Vector3 pos;
    private String pointId;

    public FreeSpawnPoint(Vector3 pos, String pointId) {
        this.pos = pos;
        this.pointId = pointId;
    }

    public Vector3 getPos() {
        return this.pos;
    }

    public void setPos(Vector3 pos) {
        this.pos = pos;
    }

    public String getPointId() {
        return this.pointId;
    }

    public void setPointId(String pointId) {
        this.pointId = pointId;
    }
}

