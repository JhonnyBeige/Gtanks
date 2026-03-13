/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.battles.dom;

import gtanks.battles.BattlefieldPlayerController;
import gtanks.battles.tanks.math.Vector3;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class DominationPoint {
    private String id;
    private Vector3 pos;
    private double radius;
    private int countRed;
    private int countBlue;
    private double score;
    private double timeLeft;
    private boolean pointCapturedByBlue;
    private boolean pointCapturedByRed;
    private List<String> userIds = new ArrayList<String>();
    private List<BattlefieldPlayerController> blues;
    private List<BattlefieldPlayerController> reds;
    private DominationModel.DominationPointHandler tickableHandler;

    public DominationPoint(String id, Vector3 pos, double radius) {
        this.id = id;
        this.pos = pos;
        this.radius = radius;
        this.blues = new CopyOnWriteArrayList<BattlefieldPlayerController>();
        this.reds = new CopyOnWriteArrayList<BattlefieldPlayerController>();
    }

    public List<BattlefieldPlayerController> getBlues() {
        return this.blues;
    }

    public List<BattlefieldPlayerController> getReds() {
        return this.reds;
    }

    public boolean isPointCapturedByBlue() {
        return this.pointCapturedByBlue;
    }

    public void setPointCapturedByBlue(boolean pointCapturedByBlue) {
        this.pointCapturedByBlue = pointCapturedByBlue;
    }

    public boolean isPointCapturedByRed() {
        return this.pointCapturedByRed;
    }

    public void setPointCapturedByRed(boolean pointCapturedByRed) {
        this.pointCapturedByRed = pointCapturedByRed;
    }

    public synchronized int getCountRed() {
        return this.countRed;
    }

    public synchronized void setCountRed(int countRed) {
        this.countRed = countRed;
    }

    public synchronized int getCountBlue() {
        return this.countBlue;
    }

    public synchronized void setCountBlue(int countBlue) {
        this.countBlue = countBlue;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Vector3 getPos() {
        return this.pos;
    }

    public void setPos(Vector3 pos) {
        this.pos = pos;
    }

    public double getRadius() {
        return this.radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public double getScore() {
        return this.score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public double getTimeLeft() {
        return this.timeLeft;
    }

    public void setTimeLeft(double d) {
        this.timeLeft = d;
    }

    public synchronized void addPlayer(String team, BattlefieldPlayerController user) {
        if (team.equals("BLUE")) {
            if (this.blues.contains(user)) {
                return;
            }
            this.setCountBlue(this.getCountBlue() + 1);
            this.blues.add(user);
        } else {
            if (this.blues.contains(user)) {
                return;
            }
            this.setCountRed(this.getCountRed() + 1);
            this.reds.add(user);
        }
        this.userIds.add(user.getUser().getNickname());
    }

    public synchronized void removePlayer(String team, BattlefieldPlayerController user) {
        if (team.equals("BLUE")) {
            if (this.getCountBlue() > 0 && this.blues.contains(user)) {
                this.setCountBlue(this.getCountBlue() - 1);
            }
            this.blues.remove(user);
        } else {
            if (this.getCountRed() > 0 && this.reds.contains(user)) {
                this.setCountRed(this.getCountRed() - 1);
            }
            this.reds.remove(user);
        }
        this.userIds.remove(user.getUser().getNickname());
    }

    public List<String> getUserIds() {
        return this.userIds;
    }

    public DominationModel.DominationPointHandler getTickableHandler() {
        return this.tickableHandler;
    }

    public void setTickableHandler(DominationModel.DominationPointHandler tickableHandler) {
        this.tickableHandler = tickableHandler;
    }
}

