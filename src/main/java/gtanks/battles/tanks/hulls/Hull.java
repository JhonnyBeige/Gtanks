/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.battles.tanks.hulls;

public class Hull {
    public float mass;
    public float power;
    public float speed;
    public float turnSpeed;
    public float hp;

    public Hull(float mass, float power, float speed, float turnSpeed, float hp) {
        this.mass = mass;
        this.power = power;
        this.speed = speed;
        this.turnSpeed = turnSpeed;
        this.hp = hp;
    }
}

