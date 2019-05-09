package com.company.utils;

import com.company.models.Vartotojas;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;

public class HibernateUtil {
    private static SessionFactory sessionFactory;

    static {
        Configuration conf = new Configuration();
        conf.addAnnotatedClass(Vartotojas.class);
        conf.setProperty("hibernate.connection.username", "root");
        conf.setProperty("hibernate.connection.password", "");
        conf.setProperty("connection.driver_class", "com.mysql.jdbc.Driver");
        conf.setProperty("hibernate.connection.url", "jdbc:mysql://localhost:3306/duombaze");
        conf.setProperty("hibernate.connection.pool_size", "10");
        conf.setProperty("show_sql", "true");
        conf.setProperty("dialect", "org.hibernate.dialect.MySQLDialect");
        conf.setProperty("hibernate.hbm2ddl.auto", "update"); // Automatically creates new tables
        StandardServiceRegistryBuilder builder = new StandardServiceRegistryBuilder().applySettings(conf.getProperties());
        sessionFactory = conf.buildSessionFactory(builder.build());
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }
}
