package ru.examples.hibernateExample.dbService.dao;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import ru.examples.hibernateExample.dbService.dataSets.UsersDataSet;

public class UsersDAO {

    private Session session;

    public UsersDAO(Session session) {
        this.session = session;
    }

    public UsersDataSet get(long id) throws HibernateException {
        return (UsersDataSet) session.get(UsersDataSet.class, id);
    }

    public long getUserId(String name) throws HibernateException {
        long id = 0l;
        Criteria criteria = session.createCriteria(UsersDataSet.class);
        UsersDataSet usersDataSet = (UsersDataSet) criteria.add(Restrictions.eq("name", name)).uniqueResult();
        if (usersDataSet != null){
            id = usersDataSet.getId();
        }
        return id;
    }

    public long insertUser(String name, String password) throws HibernateException {
        return (Long) session.save(new UsersDataSet(name, password));
    }

    public long updateUser(String name, String password) throws HibernateException {
        long id = 0l;
        Criteria criteria = session.createCriteria(UsersDataSet.class);
        UsersDataSet usersDataSet = (UsersDataSet) criteria.add(Restrictions.eq("name", name)).uniqueResult();
        if (usersDataSet != null){
            id = usersDataSet.getId();
            usersDataSet.setPassword(password);
            usersDataSet.setDateUpdate(System.currentTimeMillis());
        }
//        session.update(new UsersDataSet(name, password)); // ???
        return id;
    }

}
