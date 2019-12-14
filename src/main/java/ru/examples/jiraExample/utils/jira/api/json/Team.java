package ru.examples.jiraExample.utils.jira.api.json;

import java.net.URL;

public class Team {
    
    private int id;
    private String name;
    private URL self;

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the self
     */
    public URL getSelf() {
        return self;
    }

    /**
     * @param self the self to set
     */
    public void setSelf(URL self) {
        this.self = self;
    }
    
    @Override
    public String toString() {
        return "Team{" + "id=" + id + ", name=" + name + ", self=" + self + '}';
    }
}
