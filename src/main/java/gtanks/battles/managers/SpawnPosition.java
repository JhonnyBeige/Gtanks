/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.battles.managers;

import gtanks.battles.tanks.math.Vector3;

public class SpawnPosition {
    public Vector3 position;
    public Vector3 orintation;

    public SpawnPosition(Vector3 position, Vector3 orintation) {
        this.position = position;
        this.orintation = orintation;
    }
}

