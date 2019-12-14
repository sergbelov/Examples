package ru.examples.jiraExample.utils.jira.api.json;

public class SessionData {
    
    private Session session;
    private LoginInfo loginInfo;

    /**
     * @return the session
     */
    public Session getSession() {
        return session;
    }

    /**
     * @param session the session to set
     */
    public void setSession(Session session) {
        this.session = session;
    }

    /**
     * @return the loginInfo
     */
    public LoginInfo getLoginInfo() {
        return loginInfo;
    }

    /**
     * @param loginInfo the loginInfo to set
     */
    public void setLoginInfo(LoginInfo loginInfo) {
        this.loginInfo = loginInfo;
    }
    
    @Override
    public String toString() {
        return '[' + "session=" + session + ", loginInfo=" + loginInfo + ']';
    }
}
