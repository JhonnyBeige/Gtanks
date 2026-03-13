/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.battles.tanks.statistic;

public class PlayerStatistic
implements Comparable<PlayerStatistic> {
    private long kills;
    private int deaths;
    private int prize;
    private long score;

    public PlayerStatistic(int kills, int deaths, int score) {
        this.kills = kills;
        this.deaths = deaths;
        this.score = score;
    }

    public void addKills(boolean killsEqualsScore) {
        ++this.kills;
        if (killsEqualsScore) {
            this.score = this.kills;
        }
    }

    public void addDeaths() {
        ++this.deaths;
    }

    public void addScore(int value) {
        this.score += (long)value;
    }

    public void setScore(long value) {
        this.score = value;
    }

    public long getScore() {
        return this.score;
    }

    public long getKills() {
        return this.kills;
    }

    public int getDeaths() {
        return this.deaths;
    }

    public int getPrize() {
        return this.prize;
    }

    public void setPrize(int prize) {
        this.prize = prize;
    }

    public void clear() {
        this.kills = 0L;
        this.deaths = 0;
        this.prize = 0;
        this.score = 0L;
    }

    public String toString() {
        return "score: " + this.score + " kills: " + this.kills + " deaths: " + this.deaths + " prize: " + this.prize;
    }

    @Override
    public int compareTo(PlayerStatistic arg0) {
        return (int)(arg0.getScore() - this.score);
    }
}

