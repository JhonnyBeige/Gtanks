/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.battles.maps.parser.map;

import gtanks.battles.maps.parser.map.bonus.BonusRegion;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="bonus-regions")
class BonusRegions {
    private List<BonusRegion> bonusRegions;

    BonusRegions() {
    }

    public List<BonusRegion> getBonusRegions() {
        return this.bonusRegions;
    }

    @XmlElement(name="bonus-region")
    public void setBonusRegions(List<BonusRegion> bonusRegions) {
        this.bonusRegions = bonusRegions;
    }
}

