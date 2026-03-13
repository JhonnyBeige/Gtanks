/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.main.database;

import gtanks.logger.remote.LogObject;
import gtanks.main.netty.blackip.BlackIP;
import gtanks.rmi.payments.mapping.Payment;
import gtanks.system.news.objects.UserViewedNewsInfo;
import gtanks.users.User;
import gtanks.users.garage.Garage;
import gtanks.users.karma.Karma;
import java.util.List;

public interface DatabaseManager {
    public User getUserById(String var1);

    public User getUserByIdFromCache(String var1);

    public Garage getGarageByUser(User var1);

    public Karma getKarmaByUser(User var1);

    public BlackIP getBlackIPbyAddress(String var1);

    public List<LogObject> collectLogs();

    public void update(User var1);

    public void update(Garage var1);

    public void update(Karma var1);

    public void register(User var1);

    public void register(BlackIP var1);

    public void setPlace(User var1);

    public List<String> getUsernamesWithSameIP(String var1);

    public void register(LogObject var1);

    public void unregister(BlackIP var1);

    public void cache(User var1);

    public void uncache(String var1);

    public boolean contains(String var1);

    public UserViewedNewsInfo getUserViewedNews(String var1);


    public void register(UserViewedNewsInfo var1);

    public void update(UserViewedNewsInfo var1);

    public String initHallOfFameData();

    public Payment getPaymentById(long var1);

    public void update(Payment var1);

    public boolean checkIfEmailExists(String var1);

    public User getUserByEmail(String var1);
}

