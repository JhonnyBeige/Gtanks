/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.battles.tanks;

import gtanks.battles.BattlefieldPlayerController;
import gtanks.battles.effects.Effect;
import gtanks.battles.effects.EffectType;
import gtanks.battles.tanks.colormaps.Colormap;
import gtanks.battles.tanks.data.DamageTankData;
import gtanks.battles.tanks.hulls.Hull;
import gtanks.battles.tanks.math.Vector3;
import gtanks.battles.tanks.weapons.IWeapon;
import gtanks.battles.tanks.weapons.flamethrower.effects.FlamethrowerEffectModel;
import gtanks.battles.tanks.weapons.frezee.effects.FrezeeEffectModel;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class Tank {
    public Vector3 position;
    public Vector3 orientation;
    public Vector3 linVel;
    public Vector3 angVel;
    public double turretDir;
    public int controllBits;
    private IWeapon weapon;
    private Hull hull;
    private Colormap colormap;
    public String id;
    public float speed;
    public float turnSpeed;
    public float turretRotationSpeed;
    public int health = 10000;
    public String state = "active";
    public FrezeeEffectModel frezeeEffect;
    public FlamethrowerEffectModel flameEffect;
    public ArrayList<Effect> activeEffects;
    public LinkedHashMap<BattlefieldPlayerController, DamageTankData> lastDamagers;

    public Tank(Vector3 position) {
        this.position = position;
        this.activeEffects = new ArrayList();
        this.lastDamagers = new LinkedHashMap();
    }

    public IWeapon getWeapon() {
        return this.weapon;
    }

    public Hull getHull() {
        return this.hull;
    }

    public void setWeapon(IWeapon weapon) {
        this.weapon = weapon;
        this.turretRotationSpeed = weapon.getEntity().getShotData().turretRotationSpeed;
    }

    public void setHull(Hull hull) {
        this.hull = hull;
        this.speed = hull.speed;
        this.turnSpeed = hull.turnSpeed;
    }

    public Colormap getColormap() {
        return this.colormap;
    }

    public void setColormap(Colormap colormap) {
        this.colormap = colormap;
    }

    public boolean isUsedEffect(EffectType type) {
        for (Effect effect : this.activeEffects) {
            if (effect.getEffectType() != type) continue;
            return true;
        }
        return false;
    }
}

