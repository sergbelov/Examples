package ru.examples.jiraExample.utils.jira.api.json;

import java.net.URL;

public class SimpleUser extends User {
    
    //URL аватарки
    private URL avatar;

    /**
     * @return the avatar
     */
    public URL getAvatar() {
        return avatar;
    }

    /**
     * @param avatar the avatar to set
     */
    public void setAvatar(URL avatar) {
        this.avatar = avatar;
    }
    
    @Override
    public String toString() {
        return "SimpleUser{" + "avatar=" + avatar + ", " + super.toString() + '}';
    }
}
