/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.battles.maps.parser.map.keypoints;

import gtanks.battles.maps.parser.Vector3d;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

public class DOMKeypoint {
    private String pointId;
    private String free;
    private Vector3d position;

    @XmlAttribute(name="name")
    public String getPointId() {
        return this.pointId;
    }

    public void setPointId(String pointId) {
        this.pointId = pointId;
    }

    @XmlElement(name="position")
    public Vector3d getPosition() {
        return this.position;
    }

    public void setPosition(Vector3d position) {
        this.position = position;
    }

    @XmlAttribute(name="free")
    public String getFree() {
        return this.free;
    }

    public void setFree(String free) {
        this.free = free;
    }

    public String toString() {
        return "Point id: " + this.pointId + " position: " + String.valueOf(this.position);
    }
}

