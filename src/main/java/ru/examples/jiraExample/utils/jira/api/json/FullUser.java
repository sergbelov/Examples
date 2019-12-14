package ru.examples.jiraExample.utils.jira.api.json;

import java.net.URL;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class FullUser extends User {
    
    private String active;
    private Reserve applicationRoles;
    private Map<String, URL> avatarUrls; //<разрешение, url на аватарку>
    private String emailAddress;
    private String expand;
    private Reserve groups;
    private String key;
    private Locale locale; //en_UK
    private TimeZone timeZone;

    /**
     * @return the active
     */
    public String getActive() {
        return active;
    }

    /**
     * @param active the active to set
     */
    public void setActive(String active) {
        this.active = active;
    }

    /**
     * @return the applicationRoles
     */
    public Reserve getApplicationRoles() {
        return applicationRoles;
    }

    /**
     * @param applicationRoles the applicationRoles to set
     */
    public void setApplicationRoles(Reserve applicationRoles) {
        this.applicationRoles = applicationRoles;
    }

    /**
     * @return the avatarUrls
     */
    public Map<String, URL> getAvatarUrls() {
        return avatarUrls;
    }

    /**
     * @param avatarUrls the avatarUrls to set
     */
    public void setAvatarUrls(Map<String, URL> avatarUrls) {
        this.avatarUrls = avatarUrls;
    }

    /**
     * @return the emailAddress
     */
    public String getEmailAddress() {
        return emailAddress;
    }

    /**
     * @param emailAddress the emailAddress to set
     */
    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    /**
     * @return the expand
     */
    public String getExpand() {
        return expand;
    }

    /**
     * @param expand the expand to set
     */
    public void setExpand(String expand) {
        this.expand = expand;
    }

    /**
     * @return the groups
     */
    public Reserve getGroups() {
        return groups;
    }

    /**
     * @param groups the groups to set
     */
    public void setGroups(Reserve groups) {
        this.groups = groups;
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

    /**
     * @return the locale
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * @param locale the locale to set
     */
    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    /**
     * @return the timeZone
     */
    public TimeZone getTimeZone() {
        return timeZone;
    }

    /**
     * @param timeZone the timeZone to set
     */
    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }
    
    @Override
    public String toString() {
        return "FullUser{" + "active=" + active + ", applicationRoles=" + applicationRoles + ", avatarUrls=" + avatarUrls + ", emailAddress=" + emailAddress + ", expand=" + expand + ", groups=" + groups + ", key=" + key + ", locale=" + locale + ", timeZone=" + timeZone + ", " + super.toString() + '}';
    }
}
