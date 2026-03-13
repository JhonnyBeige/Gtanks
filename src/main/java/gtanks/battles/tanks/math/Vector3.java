/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.battles.tanks.math;

public class Vector3 {
    public float x = 0.0f;
    public float y = 0.0f;
    public float z = 0.0f;
    public double rot = 0.0;

    public Vector3(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double distanceTo(Vector3 to) {
        return Math.sqrt(this.pow2(this.x - to.x) + this.pow2(this.y - to.y) + this.pow2(this.z - to.z));
    }

    public double distanceToWithoutZ(Vector3 to) {
        return Math.sqrt(this.pow2(this.x - to.x) + this.pow2(this.y - to.y));
    }

    private double pow2(double value) {
        return Math.pow(value, 2.0);
    }
}

