package ru.examples.jiraExample.utils.jira.api.json;

import java.util.Date;

public class LoginInfo {
    
    private int loginCount;
    private int failedLoginCount;
    private Date previousLoginTime;
    private Date lastFailedLoginTime;

    /**
     * @return the loginCount
     */
    public int getLoginCount() {
        return loginCount;
    }

    /**
     * @param loginCount the loginCount to set
     */
    public void setLoginCount(int loginCount) {
        this.loginCount = loginCount;
    }

    /**
     * @return the failedLoginCount
     */
    public int getFailedLoginCount() {
        return failedLoginCount;
    }

    /**
     * @param failedLoginCount the failedLoginCount to set
     */
    public void setFailedLoginCount(int failedLoginCount) {
        this.failedLoginCount = failedLoginCount;
    }

    /**
     * @return the previousLoginTime
     */
    public Date getPreviousLoginTime() {
        return previousLoginTime;
    }

    /**
     * @param previousLoginTime the previousLoginTime to set
     */
    public void setPreviousLoginTime(Date previousLoginTime) {
        this.previousLoginTime = previousLoginTime;
    }

    /**
     * @return the lastFailedLoginTime
     */
    public Date getLastFailedLoginTime() {
        return lastFailedLoginTime;
    }

    /**
     * @param lastFailedLoginTime the lastFailedLoginTime to set
     */
    public void setLastFailedLoginTime(Date lastFailedLoginTime) {
        this.lastFailedLoginTime = lastFailedLoginTime;
    }
    
    @Override
    public String toString() {
        return '[' + "loginCount=" + loginCount + ", failedLoginCount=" + failedLoginCount + ", previousLoginTime=" + previousLoginTime + ", lastFailedLoginTime=" + lastFailedLoginTime + ']';
    }
    
}
