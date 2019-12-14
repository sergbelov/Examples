package ru.examples.hibernateExample.dbService.dataSets;

import javax.persistence.*;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

@Entity
@Table(name = "users")
public class UsersDataSet implements Serializable { // Serializable Important to Hibernate!
//    private static final long serialVersionUID = -1111111111111111111L;

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "name", unique = true, updatable = false)
    private String name;

    @Column(name = "password")
    private String password;

    @Column(name = "date_create", updatable = false)
    private long dateCreate;

    @Column(name = "date_update")
    private long dateUpdate;



    public UsersDataSet() {
    }

    public UsersDataSet(long id, String name, String password) {
        this.setId(id);
        this.setName(name);
        this.setPassword(password);
        this.setDateCreate(System.currentTimeMillis());
        this.setDateUpdate(System.currentTimeMillis());
    }

    public UsersDataSet(String name, String password) {
        this.setId(-1);
        this.setName(name);
        this.setPassword(password);
        this.setDateCreate(System.currentTimeMillis());
        this.setDateUpdate(System.currentTimeMillis());
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() { return password; }

    public void setPassword(String password) { this.password = password; }

    public long getDateCreate() { return dateCreate; }

    public void setDateCreate(long dateCreate) { this.dateCreate = dateCreate; }

    public long getDateUpdate() { return dateUpdate; }

    public void setDateUpdate(long dateUpdate) { this.dateUpdate = dateUpdate; }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");

        return "UserDataSet{" +
                "id=" + id +
                ", name='" + name +
                "', password='" + password +
//                ", date_create=" + dateCreate +
                "', date_create='" + dateFormat.format(dateCreate) +
                "', date_update='" + dateFormat.format(dateUpdate) +
                "'}";
    }
}