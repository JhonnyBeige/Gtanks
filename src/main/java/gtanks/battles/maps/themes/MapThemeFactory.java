/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.battles.maps.themes;

import gtanks.battles.maps.themes.MapTheme;

public class MapThemeFactory {
    public static MapTheme getDefaultMapTheme() {
        return new MapTheme(){
            {
                this.setAmbientSoundId("default_ambient_sound");
                this.setGameModeId("default");
            }
        };
    }

    public static MapTheme getMapTheme(final String soundId, final String gameMode) {
        return new MapTheme(){
            {
                this.setAmbientSoundId(soundId);
                this.setGameModeId(gameMode);
            }
        };
    }
}

