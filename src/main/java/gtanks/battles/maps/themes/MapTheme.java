/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.battles.maps.themes;

public class MapTheme {
    private String gameModeId;
    private String ambientSoundId;

    public String getAmbientSoundId() {
        return this.ambientSoundId;
    }

    protected void setAmbientSoundId(String ambientSoundId) {
        this.ambientSoundId = ambientSoundId;
    }

    public String getGameModeId() {
        return this.gameModeId;
    }

    protected void setGameModeId(String gameModeId) {
        this.gameModeId = gameModeId;
    }
}

