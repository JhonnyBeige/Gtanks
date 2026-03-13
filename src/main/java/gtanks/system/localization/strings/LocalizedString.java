/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.system.localization.strings;

import gtanks.system.localization.Localization;
import java.util.HashMap;
import java.util.Map;

public class LocalizedString {
    private final Map<Localization, String> localizatedMap = new HashMap<Localization, String>();

    protected LocalizedString(String ruVersion, String enVersion) {
        this.localizatedMap.put(Localization.RU, ruVersion);
        this.localizatedMap.put(Localization.EN, enVersion);
    }

    public String localizatedString(Localization loc) {
        String string = this.localizatedMap.get((Object)loc);
        return string == null ? "null" : string;
    }
}

