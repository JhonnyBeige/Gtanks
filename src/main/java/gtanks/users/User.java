/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.users;

import gtanks.network.Session;
import gtanks.system.localization.Localization;
import gtanks.users.TypeUser;
import gtanks.users.anticheat.AntiCheatData;
import gtanks.users.garage.Garage;
import gtanks.users.groups.UserGroup;
import gtanks.users.karma.Karma;
import gtanks.users.locations.UserLocation;
import gtanks.utils.StringUtils;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@org.hibernate.annotations.Entity
@Table(name="users")
public class User
implements Serializable {
    private static final long serialVersionUID = 1594026136266908606L;
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="uid", unique=true, nullable=false)
    private long id;
    @Column(name="nickname", unique=true, nullable=false)
    private String nickname;
    @Column(name="password", unique=true, nullable=false)
    private String password;
    @Column(name="rank", unique=true, nullable=false)
    private int rang = 0;
    @Column(name="score", unique=true, nullable=false)
    private int score = 0;
    @Column(name="crystalls", unique=true, nullable=false)
    private int crystall = 0;
    @Column(name="next_score", unique=true, nullable=false)
    private int nextScore = 100;
    @Column(name="place", unique=true, nullable=false)
    private int place = 0;
    @Column(name="rating", unique=true, nullable=false)
    private int rating = 1;
    @Column(name="email", unique=true, nullable=true)
    private String email = "default@gtanks.com";
    @Column(name="last_ip", unique=false, nullable=false)
    private String lastIP;
    @Column(name="user_type", unique=true, nullable=false)
    @Enumerated(value=EnumType.ORDINAL)
    private TypeUser type = TypeUser.DEFAULT;
    @Column(name="last_issue_bonus", nullable=true)
    private Date lastIssueBonus;
    @Column(name="purchasedFirstThing", unique=true, nullable=false)
    private boolean firstPurchase;
    @Column(name="kills", unique=true, nullable=false)
    private int kills = 0;
    @Column(name="deaths", unique=true, nullable=false)
    private int deaths = 0;
    @Column(name="kd", unique=true, nullable=false)
    private double kd = 0.0;
    @Column(name="wealth", unique=true, nullable=false)
    private int wealth = 0;
    @Transient
    private UserLocation userLocation;
    @Transient
    private Karma karma;
    @Transient
    private Garage garage;
    @Transient
    private int warnings;
    @Transient
    private final AntiCheatData antiCheatData = new AntiCheatData();
    @Transient
    public Session session;
    @Transient
    private UserGroup userGroup;
    @Transient
    private Localization localization;

    public User(String nickname, String password) {
        this.nickname = nickname;
        this.password = password;
        this.garage = new Garage();
    }

    public User() {
    }

    public String getNickname() {
        return this.nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getRang() {
        return this.rang;
    }

    public void setRang(int rang) {
        this.rang = rang;
    }

    public int getKills() {
        return this.kills;
    }

    public void setKills(int kills) {
        this.kills = kills;
    }

    public int getDeaths() {
        return this.deaths;
    }

    public void setDeaths(int deaths) {
        this.deaths = deaths;
    }

    public double getKd() {
        return this.kd;
    }

    public void setKd(double kd) {
        this.kd = !Double.isNaN(kd) ? kd : 0.0;
    }

    public int getScore() {
        return this.score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void addScore(int score) {
        this.score += score;
    }

    public int getCrystall() {
        return this.crystall;
    }

    public void setCrystall(int crystall) {
        this.crystall = crystall;
    }

    public void addCrystall(int crystall) {
        this.crystall += crystall;
    }

    public int getNextScore() {
        return this.nextScore;
    }

    public void setNextScore(int nextScore) {
        this.nextScore = nextScore;
    }

    public int getPlace() {
        return this.place;
    }

    public void setPlace(int userPlace) {
        this.place = userPlace;
    }

    public int getRating() {
        this.rating = (int)((float)Math.sqrt((float)this.score * (float)this.kd) + (float)this.wealth);
        return this.rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public int getWealth() {
        return this.wealth;
    }

    public void setWealth(int wealth) {
        this.wealth = wealth;
    }

    public Garage getGarage() {
        return this.garage;
    }

    public void setGarage(Garage garage) {
        this.garage = garage;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public TypeUser getType() {
        return this.type;
    }

    public void setType(TypeUser type) {
        this.type = type;
    }

    public UserLocation getUserLocation() {
        return this.userLocation;
    }

    public void setUserLocation(UserLocation userLocation) {
        this.userLocation = userLocation;
    }

    public Karma getKarma() {
        return this.karma;
    }

    public void setKarma(Karma karma) {
        this.karma = karma;
    }

    public int getWarnings() {
        return this.warnings;
    }

    public void setWarnings(int warnings) {
        this.warnings = warnings;
    }

    public void addWarning() {
        ++this.warnings;
    }

    public AntiCheatData getAntiCheatData() {
        return this.antiCheatData;
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getLastIP() {
        return this.lastIP;
    }

    public void setLastIP(String lastIP) {
        this.lastIP = lastIP;
    }

    public UserGroup getUserGroup() {
        return this.userGroup;
    }

    public void setUserGroup(UserGroup userGroup) {
        this.userGroup = userGroup;
    }

    public Date getLastIssueBonus() {
        return this.lastIssueBonus;
    }

    public void setLastIssueBonus(Date lastIssueBonus) {
        this.lastIssueBonus = lastIssueBonus;
    }

    public boolean getFirstPurchase() {
        return this.firstPurchase;
    }

    public void setFirstPurchase(Boolean firstPurchase) {
        this.firstPurchase = firstPurchase;
    }

    public String toString() {
        return StringUtils.concatStrings(String.valueOf(this.rang), " ", this.nickname, " ", this.password);
    }

    public Localization getLocalization() {
        return this.localization;
    }

    public void setLocalization(Localization localization) {
        this.localization = localization;
    }
}

