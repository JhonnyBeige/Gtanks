/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.services.hibernate;

import gtanks.utils.ResourceUtils;
import java.io.File;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateService {
    private static final String HIBERNATE_CLASSPATH_CONFIG = "config/hibernate.cfg.xml";
    private static SessionFactory sessionFactory = HibernateService.createSessionFactory();

    private static SessionFactory createSessionFactory() {
        try {
            return new Configuration().configure(HIBERNATE_CLASSPATH_CONFIG).buildSessionFactory();
        } catch (Exception ignored) {
            return new Configuration().configure(new File(ResourceUtils.config("hibernate.cfg.xml"))).buildSessionFactory();
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }
}
