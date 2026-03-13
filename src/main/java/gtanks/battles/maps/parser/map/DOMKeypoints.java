/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.battles.maps.parser.map;

import gtanks.battles.maps.parser.map.keypoints.DOMKeypoint;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="dom-keypoints")
class DOMKeypoints {
    private List<DOMKeypoint> points = new ArrayList<DOMKeypoint>();

    DOMKeypoints() {
    }

    @XmlElement(name="dom-keypoint")
    public List<DOMKeypoint> getPoints() {
        return this.points;
    }

    public void setBonusRegions(List<DOMKeypoint> points) {
        this.points = points;
    }
}

