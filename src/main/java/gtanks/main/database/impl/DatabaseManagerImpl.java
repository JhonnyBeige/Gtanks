/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.main.database.impl;

import gtanks.logger.Logger;
import gtanks.logger.Type;
import gtanks.logger.remote.LogObject;
import gtanks.logger.remote.RemoteDatabaseLogger;
import gtanks.main.database.DatabaseManager;
import gtanks.main.netty.blackip.BlackIP;
import gtanks.rmi.payments.mapping.Payment;
import gtanks.services.hibernate.HibernateService;
import gtanks.system.news.objects.UserViewedNewsInfo;
import gtanks.users.TypeUser;
import gtanks.users.User;
import gtanks.users.garage.Garage;
import gtanks.users.karma.Karma;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class DatabaseManagerImpl
extends Thread
implements DatabaseManager {
    private static final DatabaseManagerImpl instance = new DatabaseManagerImpl();
    private final Map<String, User> cache = new TreeMap<String, User>(String.CASE_INSENSITIVE_ORDER);

    private DatabaseManagerImpl() {
        super("DatabaseManagerImpl THREAD");
    }

    @Override
    public void register(User user) {
        this.configurateNewAccount(user);
        Garage garage = new Garage();
        garage.parseJSONData();
        garage.setUserId(user.getNickname());
        Karma emptyKarma = new Karma();
        emptyKarma.setUserId(user.getNickname());
        Session session = null;
        try {
            session = HibernateService.getSessionFactory().getCurrentSession();
            Transaction tx = session.beginTransaction();
            this.removeOrphanAccountData(session, user.getNickname());
            session.save(user);
            session.save(garage);
            session.save(emptyKarma);
            tx.commit();
        } catch (Exception e) {
            if (session.getTransaction() != null) {
                session.getTransaction().rollback();
            }
            e.printStackTrace();
            RemoteDatabaseLogger.error(e);
        }
    }

    private void removeOrphanAccountData(Session session, String nickname) {
        Query deleteGarage = session.createQuery("DELETE FROM Garage G WHERE G.userId = :nickname");
        deleteGarage.setParameter("nickname", (Object)nickname);
        deleteGarage.executeUpdate();
        Query deleteKarma = session.createQuery("DELETE FROM Karma K WHERE K.userId = :nickname");
        deleteKarma.setParameter("nickname", (Object)nickname);
        deleteKarma.executeUpdate();
    }

    @Override
    public Payment getPaymentById(long paymentId) {
        Session session = null;
        Payment payment = null;
        try {
            session = HibernateService.getSessionFactory().getCurrentSession();
            session.beginTransaction();
            Query query = session.createQuery("FROM Payment WHERE id_payment = :pid");
            query.setParameter("pid", (Object)paymentId);
            payment = (Payment)query.uniqueResult();
            session.getTransaction().commit();
        } catch (Exception e) {
            e.printStackTrace();
            RemoteDatabaseLogger.error(e);
        }
        return payment;
    }

    @Override
    public void update(Payment payment) {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateService.getSessionFactory().getCurrentSession();
            tx = session.beginTransaction();
            session.update(payment);
            tx.commit();
        } catch (Exception var5) {
            var5.printStackTrace();
            RemoteDatabaseLogger.error(var5);
        }
    }

    @Override
    public String initHallOfFameData() {
        List<User> usersFromDatabase = this.getAllUsersOrderedByRating();
        String jsonString = null;
        if (usersFromDatabase != null && !usersFromDatabase.isEmpty()) {
            Collections.sort(usersFromDatabase, Comparator.comparing(User::getRating).reversed());
            JSONArray usersArray = new JSONArray();
            for (User user : usersFromDatabase) {
                JSONObject userData = new JSONObject();
                userData.put("userName", user.getNickname());
                JSONArray awardsArray = new JSONArray();
                awardsArray.add("GTANKS BETA TESTER");
                userData.put("awards", awardsArray);
                userData.put("rank", user.getRang() + 1);
                userData.put("userRating", user.getRating());
                usersArray.add(userData);
            }
            JSONObject jsonData = new JSONObject();
            jsonData.put("users", usersArray);
            jsonString = jsonData.toJSONString();
        }
        return jsonString;
    }

    private List<User> getAllUsersOrderedByRating() {
        List usersFromDatabase = null;
        Session session = null;
        try {
            session = HibernateService.getSessionFactory().getCurrentSession();
            session.beginTransaction();
            CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
            CriteriaQuery<User> criteriaQuery = criteriaBuilder.createQuery(User.class);
            Root<User> root = criteriaQuery.from(User.class);
            criteriaQuery.select(root).orderBy(criteriaBuilder.desc(root.get("rating")));
            usersFromDatabase = session.createQuery((CriteriaQuery)criteriaQuery).getResultList();
            session.getTransaction().commit();
        } catch (Exception e) {
            if (session != null && session.getTransaction() != null && session.getTransaction().isActive()) {
                session.getTransaction().rollback();
            }
            e.printStackTrace();
            RemoteDatabaseLogger.error(e);
        }
        return usersFromDatabase;
    }

    @Override
    public void update(Karma karma) {
        Session session = null;
        Transaction tx = null;
        User user = null;
        user = this.cache.get(karma.getUserId());
        if (user != null) {
            user.setKarma(karma);
        }
        try {
            session = HibernateService.getSessionFactory().getCurrentSession();
            tx = session.beginTransaction();
            session.update(karma);
            tx.commit();
        } catch (Exception e) {
            if (session.getTransaction() != null) {
                session.getTransaction().rollback();
            }
            e.printStackTrace();
            RemoteDatabaseLogger.error(e);
        }
    }

    @Override
    public void update(Garage garage) {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateService.getSessionFactory().getCurrentSession();
            tx = session.beginTransaction();
            session.update(garage);
            tx.commit();
        } catch (Exception e) {
            if (session.getTransaction() != null) {
                session.getTransaction().rollback();
            }
            e.printStackTrace();
            RemoteDatabaseLogger.error(e);
        }
    }

    @Override
    public void update(User user) {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateService.getSessionFactory().getCurrentSession();
            tx = session.beginTransaction();
            session.update(user);
            tx.commit();
        } catch (Exception e) {
            if (session.getTransaction() != null) {
                session.getTransaction().rollback();
            }
            e.printStackTrace();
            RemoteDatabaseLogger.error(e);
        }
    }

    @Override
    public boolean checkIfEmailExists(String email) {
        Session session = null;
        Long count = 0L;
        try {
            session = HibernateService.getSessionFactory().getCurrentSession();
            session.beginTransaction();
            TypedQuery query = session.createQuery("SELECT COUNT(*) FROM User U WHERE U.email = :email", Long.class);
            query.setParameter("email", (Object)email);
            count = (Long) ((Query<?>) query).uniqueResult();
            session.getTransaction().commit();
        } catch (Exception e) {
            if (session != null && session.getTransaction() != null && session.getTransaction().isActive()) {
                session.getTransaction().rollback();
            }
            e.printStackTrace();
            RemoteDatabaseLogger.error(e);
        }
        return count > 0L;
    }

    @Override
    public User getUserByEmail(String email) {
        Session session = null;
        Transaction tx = null;
        User user = null;
        try {
            session = HibernateService.getSessionFactory().getCurrentSession();
            tx = session.beginTransaction();
            Query query = session.createQuery("FROM User U WHERE U.email = :email");
            query.setParameter("email", (Object)email);
            user = (User)query.uniqueResult();
            tx.commit();
        } catch (Exception e) {
            if (session.getTransaction() != null) {
                session.getTransaction().rollback();
            }
            e.printStackTrace();
            RemoteDatabaseLogger.error(e);
        }
        return user;
    }

    @Override
    public User getUserById(String nickname) {
        Session session = null;
        Transaction tx = null;
        User user = null;
        user = this.getUserByIdFromCache(nickname);
        if (user != null) {
            return user;
        }
        try {
            session = HibernateService.getSessionFactory().getCurrentSession();
            tx = session.beginTransaction();
            Query query = session.createQuery("FROM User U WHERE U.nickname = :nickname");
            query.setString("nickname", nickname);
            user = (User)query.uniqueResult();
            tx.commit();
        } catch (Exception e) {
            if (session.getTransaction() != null) {
                session.getTransaction().rollback();
            }
            e.printStackTrace();
            RemoteDatabaseLogger.error(e);
        }
        return user;
    }

    public static DatabaseManager instance() {
        return instance;
    }

    @Override
    public void cache(User user) {
        if (user == null) {
            Logger.log(Type.ERROR, "DatabaseManagerImpl::cache user is null!");
            return;
        }
        this.cache.put(user.getNickname(), user);
    }

    @Override
    public void setPlace(User user) {
        Session session = null;
        try {
            session = HibernateService.getSessionFactory().getCurrentSession();
            session.beginTransaction();
            int userRating = user.getRating();
            TypedQuery query = session.createQuery("SELECT COUNT(*) FROM User U WHERE U.rating > :userRating", Long.class);
            query.setParameter("userRating", (Object)userRating);
            long userPlace = (Long)query.getSingleResult();
            user.setPlace((int)userPlace);
            session.getTransaction().commit();
        } catch (Exception e) {
            if (session != null && session.getTransaction() != null && session.getTransaction().isActive()) {
                session.getTransaction().rollback();
            }
            e.printStackTrace();
            RemoteDatabaseLogger.error(e);
        }
    }

    @Override
    public List<String> getUsernamesWithSameIP(String ipAddress) {
        List<String> list;
        block8: {
            Session session = HibernateService.getSessionFactory().getCurrentSession();
            try {
                session.beginTransaction();
                String sql = "SELECT nickname FROM users WHERE SUBSTRING_INDEX(last_ip, ':', 1) = :ip";
                NativeQuery query = session.createSQLQuery(sql);
                query.setParameter("ip", (Object)ipAddress);
                List<String> usernames = query.list();
                session.getTransaction().commit();
                list = usernames;
                if (session == null) break block8;
            } catch (Throwable throwable) {
                try {
                    if (session != null) {
                        try {
                            session.close();
                        } catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                } catch (Exception e) {
                    RemoteDatabaseLogger.error("Error while fetching usernames with the same IP: " + e.getMessage());
                    e.printStackTrace();
                    return Collections.emptyList();
                }
            }
            session.close();
        }
        return list;
    }

    @Override
    public void uncache(String id) {
        this.cache.remove(id);
    }

    @Override
    public User getUserByIdFromCache(String nickname) {
        return this.cache.get(nickname);
    }

    @Override
    public boolean contains(String nickname) {
        return this.getUserById(nickname) != null;
    }

    public void configurateNewAccount(User user) {
        if (user.getCrystall() <= 0) {
            user.setCrystall(5);
        }
        if (user.getNextScore() <= 0 && user.getRang() == 0) {
            user.setNextScore(100);
        }
        if (user.getType() == null) {
            user.setType(TypeUser.DEFAULT);
        }
        user.setEmail(null);
        user.setFirstPurchase(true);
    }

    @Override
    public Garage getGarageByUser(User user) {
        Session session = null;
        Transaction tx = null;
        Garage garage = null;
        try {
            session = HibernateService.getSessionFactory().getCurrentSession();
            tx = session.beginTransaction();
            Query query = session.createQuery("FROM Garage G WHERE G.userId = :nickname");
            query.setString("nickname", user.getNickname());
            garage = (Garage)query.uniqueResult();
            tx.commit();
        } catch (Exception e) {
            if (session.getTransaction() != null) {
                session.getTransaction().rollback();
            }
            e.printStackTrace();
            RemoteDatabaseLogger.error(e);
        }
        return garage;
    }

    @Override
    public Karma getKarmaByUser(User user) {
        Session session = null;
        Transaction tx = null;
        Karma karma = null;
        if (user.getKarma() != null) {
            return user.getKarma();
        }
        try {
            session = HibernateService.getSessionFactory().getCurrentSession();
            tx = session.beginTransaction();
            Query query = session.createQuery("FROM Karma K WHERE K.userId = :nickname");
            query.setString("nickname", user.getNickname());
            karma = (Karma)query.uniqueResult();
            tx.commit();
        } catch (Exception e) {
            if (session.getTransaction() != null) {
                session.getTransaction().rollback();
            }
            e.printStackTrace();
            RemoteDatabaseLogger.error(e);
        }
        return karma;
    }

    @Override
    public BlackIP getBlackIPbyAddress(String address) {
        Session session = null;
        List blackIPs = null;
        try {
            session = HibernateService.getSessionFactory().getCurrentSession();
            session.beginTransaction();
            Query query = session.createQuery("FROM BlackIP B WHERE B.ip = :ip");
            query.setString("ip", address);
            blackIPs = query.list();
            session.getTransaction().commit();
        } catch (Exception e) {
            e.printStackTrace();
            RemoteDatabaseLogger.error(e);
        }
        if (blackIPs != null && !blackIPs.isEmpty()) {
            return (BlackIP)blackIPs.get(0);
        }
        return null;
    }

    @Override
    public void register(BlackIP blackIP) {
        Session session = null;
        Transaction tx = null;
        if (this.getBlackIPbyAddress(blackIP.getIp()) != null) {
            return;
        }
        try {
            session = HibernateService.getSessionFactory().getCurrentSession();
            tx = session.beginTransaction();
            session.saveOrUpdate(blackIP);
            tx.commit();
        } catch (Exception e) {
            if (session.getTransaction() != null) {
                session.getTransaction().rollback();
            }
            e.printStackTrace();
            RemoteDatabaseLogger.error(e);
        }
    }

    @Override
    public void unregister(BlackIP blackIP) {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateService.getSessionFactory().getCurrentSession();
            tx = session.beginTransaction();
            NativeQuery query = session.createSQLQuery("delete from GTanks.black_ips where ip = :ip");
            query.setString("ip", blackIP.getIp());
            query.executeUpdate();
            tx.commit();
        } catch (Exception e) {
            if (session.getTransaction() != null) {
                session.getTransaction().rollback();
            }
            e.printStackTrace();
            RemoteDatabaseLogger.error(e);
        }
    }

    @Override
    public void register(LogObject log) {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateService.getSessionFactory().getCurrentSession();
            tx = session.beginTransaction();
            session.save(log);
            tx.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<LogObject> collectLogs() {
        Session session = null;
        List logs = null;
        try {
            session = HibernateService.getSessionFactory().getCurrentSession();
            Transaction tx = session.beginTransaction();
            logs = session.createCriteria(LogObject.class).list();
            tx.commit();
        } catch (Exception e) {
            e.printStackTrace();
            RemoteDatabaseLogger.error(e);
            return null;
        }
        return logs;
    }

    @Override
    public UserViewedNewsInfo getUserViewedNews(String userid) {
        Session session = null;
        UserViewedNewsInfo news = null;
        try {
            session = HibernateService.getSessionFactory().getCurrentSession();
            session.beginTransaction();
            Query query = session.createQuery("FROM UserViewedNewsInfo N WHERE N.nickname = :nickname");
            query.setString("nickname", userid);
            news = (UserViewedNewsInfo)query.uniqueResult();
            session.getTransaction().commit();
        } catch (Exception var5) {
            var5.printStackTrace();
            RemoteDatabaseLogger.error(var5);
        }
        return news;
    }

    @Override
    public void register(UserViewedNewsInfo uvni) {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateService.getSessionFactory().getCurrentSession();
            tx = session.beginTransaction();
            session.save(uvni);
            tx.commit();
        } catch (Exception var5) {
            var5.printStackTrace();
        }
    }

    @Override
    public void update(UserViewedNewsInfo uv) {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateService.getSessionFactory().getCurrentSession();
            tx = session.beginTransaction();
            session.saveOrUpdate(uv);
            tx.commit();
        } catch (Exception var5) {
            var5.printStackTrace();
            RemoteDatabaseLogger.error(var5);
        }
    }
}
