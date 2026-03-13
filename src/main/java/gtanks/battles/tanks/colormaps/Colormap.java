/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.battles.tanks.colormaps;

import gtanks.battles.tanks.colormaps.ColormapResistanceType;
import gtanks.battles.tanks.weapons.EntityType;
import java.util.HashMap;

public class Colormap {
    private HashMap<ColormapResistanceType, Integer> resistances = new HashMap();

    public void addResistance(ColormapResistanceType type, int percent) {
        this.resistances.put(type, percent);
    }

    public Integer getResistance(EntityType weaponType) {
        return this.resistances.get((Object)this.getResistanceTypeByWeapon(weaponType));
    }

    private ColormapResistanceType getResistanceTypeByWeapon(EntityType weaponType) {
        ColormapResistanceType type = null;
        switch (weaponType) {
            case SMOKY: {
                type = ColormapResistanceType.SMOKY;
                break;
            }
            case FLAMETHROWER: {
                type = ColormapResistanceType.FLAMETHROWER;
                break;
            }
            case TWINS: {
                type = ColormapResistanceType.TWINS;
                break;
            }
            case RAILGUN: {
                type = ColormapResistanceType.RAILGUN;
                break;
            }
            case ISIDA: {
                type = ColormapResistanceType.ISIDA;
                break;
            }
            case THUNDER: {
                type = ColormapResistanceType.THUNDER;
                break;
            }
            case FREEZE: {
                type = ColormapResistanceType.FREZEE;
                break;
            }
            case RICOCHET: {
                type = ColormapResistanceType.RICOCHET;
                break;
            }
            case SHAFT: {
                type = ColormapResistanceType.SHAFT;
            }
        }
        return type;
    }
}

