package ru.examples.jiraExample.utils.jira.api.json;

import java.net.URL;

public class User {
    
    //Полное имя (Иванов Иван Иванович)
    private String displayName;
    //Фактически логи (Ivanov-II)
    private String name;
    //Профиль пользователя
    private URL self;

    /**
     * @return the displayName
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * @param displayName the displayName to set
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
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
        return "displayName=" + displayName + ", name=" + name + ", self=" + self;
    }
}
