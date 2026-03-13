/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.battles.tanks.colormaps;

import gtanks.battles.tanks.colormaps.Colormap;
import gtanks.battles.tanks.colormaps.ColormapResistanceType;
import gtanks.users.garage.enums.PropertyType;
import java.util.HashMap;
import java.util.Map;

public class ColormapsFactory {
    private static Map<String, Colormap> colormaps = new HashMap<String, Colormap>();

    public static void addColormap(String id, Colormap colormap) {
        colormaps.put(id, colormap);
    }

    public static Colormap getColormap(String id) {
        return colormaps.get(id);
    }

    public static ColormapResistanceType getResistanceType(PropertyType pType) {
        ColormapResistanceType type = null;
        switch (pType) {
            case FIRE_RESISTANCE: {
                type = ColormapResistanceType.FLAMETHROWER;
                break;
            }
            case FREEZE_RESISTANCE: {
                type = ColormapResistanceType.FREZEE;
                break;
            }
            case MECH_RESISTANCE: {
                type = ColormapResistanceType.SMOKY;
                break;
            }
            case PLASMA_RESISTANCE: {
                type = ColormapResistanceType.TWINS;
                break;
            }
            case RAIL_RESISTANCE: {
                type = ColormapResistanceType.RAILGUN;
                break;
            }
            case RICOCHET_RESISTANCE: {
                type = ColormapResistanceType.RICOCHET;
                break;
            }
            case THUNDER_RESISTANCE: {
                type = ColormapResistanceType.THUNDER;
                break;
            }
            case VAMPIRE_RESISTANCE: {
                type = ColormapResistanceType.ISIDA;
                break;
            }
            case SHAFT_RESISTANCE: {
                type = ColormapResistanceType.SHAFT;
                break;
            }
            case AIMING_ERROR: {
                break;
            }
            case ARMOR: {
                break;
            }
            case CONE_ANGLE: {
                break;
            }
            case DAMAGE: {
                break;
            }
            case DAMAGE_PER_SECOND: {
                break;
            }
            case HEALING_RADUIS: {
                break;
            }
            case HEAL_RATE: {
                break;
            }
            case SHOT_AREA: {
                break;
            }
            case SHOT_FREQUENCY: {
                break;
            }
            case SHOT_RANGE: {
                break;
            }
            case SPEED: {
                break;
            }
            case TURN_SPEED: {
                break;
            }
            case TURRET_TURN_SPEED: {
                break;
            }
            case UNKNOWN: {
                break;
            }
            case VAMPIRE_RATE: {
                break;
            }
            case SHAFT_DAMAGE: {
                break;
            }
            case SHAFT_SHOT_FREQUENCY: {
                break;
            }
        }
        return type;
    }
}

