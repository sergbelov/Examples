package ru.examples.hibernateExample.dbService;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.internal.SessionFactoryImpl;
import org.hibernate.service.ServiceRegistry;
import ru.examples.hibernateExample.dbService.dao.UsersDAO;
import ru.examples.hibernateExample.dbService.dataSets.UsersDataSet;

import java.sql.Connection;
import java.sql.SQLException;

public class DBService {

    private final SessionFactory sessionFactory;
/*
    validate:       проверить схему, не вносить изменения в базу данных.
    update:         обновить схему.
    create:         создает схему, уничтожая предыдущие данные.
    create-drop:    отказаться от схемы, когда SessionFactory закрывается явно, как правило, когда приложение остановлено.
*/

    public DBService(
            String hibernateShowSQL,
            String hibernateHbm2ddlAuto ) {

        Configuration configuration = getH2Configuration(
                hibernateShowSQL,
                hibernateHbm2ddlAuto);

        sessionFactory = createSessionFactory(configuration);
    }

    private Configuration getMySqlConfiguration(
            String hibernateShowSQL,
            String hibernateHbm2ddlAuto ) {

        Configuration configuration = new Configuration();
        configuration.addAnnotatedClass(UsersDataSet.class);

        configuration.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
        configuration.setProperty("hibernate.connection.driver_class", "com.mysql.jdbc.Driver");
        configuration.setProperty("hibernate.connection.url", "jdbc:mysql://localhost:3306/db_example");
        configuration.setProperty("hibernate.connection.username", "admin");
        configuration.setProperty("hibernate.connection.password", "admin");
        configuration.setProperty("hibernate.show_sql", hibernateShowSQL);
        configuration.setProperty("hibernate.hbm2ddl.auto", hibernateHbm2ddlAuto);
        return configuration;
    }

    private Configuration getH2Configuration(
            String hibernateShowSQL,
            String hibernateHbm2ddlAuto ) {

        Configuration configuration = new Configuration();
        configuration.addAnnotatedClass(UsersDataSet.class);

        configuration.setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
        configuration.setProperty("hibernate.connection.driver_class", "org.h2.Driver");
        configuration.setProperty("hibernate.connection.url", "jdbc:h2:./h2db_2");
        configuration.setProperty("hibernate.connection.username", "admin");
        configuration.setProperty("hibernate.connection.password", "admin");
        configuration.setProperty("hibernate.show_sql", hibernateShowSQL);
        configuration.setProperty("hibernate.hbm2ddl.auto", hibernateHbm2ddlAuto);
        return configuration;
    }


    public UsersDataSet getUser(long id) throws DBException {
        try {
            Session session = sessionFactory.openSession();
            UsersDAO dao = new UsersDAO(session);
            UsersDataSet dataSet = dao.get(id);
            session.close();
            return dataSet;
        } catch (HibernateException e) {
            throw new DBException(e);
        }
    }

    public long addUser(String name, String password) throws DBException {
        try {
            Session session = sessionFactory.openSession();
            Transaction transaction = session.beginTransaction();
            UsersDAO dao = new UsersDAO(session);
            long id;
            id = dao.getUserId(name);
            if (id == 0) {
                id = dao.insertUser(name, password);
                transaction.commit();
            }
            session.close();
            return id;
        } catch (HibernateException e) {
            throw new DBException(e);
        }
    }

    public boolean updateUser(String name, String password) throws DBException {
        try {
            Session session = sessionFactory.openSession();
            Transaction transaction = session.beginTransaction();
            UsersDAO dao = new UsersDAO(session);
            dao.updateUser(name, password);
            transaction.commit();
            session.close();
            return true;
        } catch (HibernateException e) {
            throw new DBException(e);
        }
    }
    public void printConnectInfo() {
        try {
            SessionFactoryImpl sessionFactoryImpl = (SessionFactoryImpl) sessionFactory;
            Connection connection = sessionFactoryImpl.getConnectionProvider().getConnection();
            System.out.println("DB name: " + connection.getMetaData().getDatabaseProductName());
            System.out.println("DB version: " + connection.getMetaData().getDatabaseProductVersion());
            System.out.println("Driver: " + connection.getMetaData().getDriverName());
            System.out.println("Autocommit: " + connection.getAutoCommit());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static SessionFactory createSessionFactory(Configuration configuration) {
        StandardServiceRegistryBuilder builder = new StandardServiceRegistryBuilder();
        builder.applySettings(configuration.getProperties());
        ServiceRegistry serviceRegistry = builder.build();
        return configuration.buildSessionFactory(serviceRegistry);
    }
}
