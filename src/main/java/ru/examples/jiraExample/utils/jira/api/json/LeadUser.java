package ru.examples.jiraExample.utils.jira.api.json;

import java.net.URL;
import java.util.Map;

public class LeadUser extends User {
    
    private Map<String, URL> avatar;
    private boolean jiraUser;
    private String key;

    /**
     * @return the avatar
     */
    public Map<String, URL> getAvatar() {
        return avatar;
    }

    /**
     * @param avatar the avatar to set
     */
    public void setAvatar(Map<String, URL> avatar) {
        this.avatar = avatar;
    }

    /**
     * @return the jiraUser
     */
    public boolean isJiraUser() {
        return jiraUser;
    }

    /**
     * @param jiraUser the jiraUser to set
     */
    public void setJiraUser(boolean jiraUser) {
        this.jiraUser = jiraUser;
    }

    /**
     * @return the key
     */
    public String getKey() {
        return key;
    }

    /**
     * @param key the key to set
     */
    public void setKey(String key) {
        this.key = key;
    }
    
}
