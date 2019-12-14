package ru.examples.jiraExample.utils.jira.api;

public enum Command {
    
    SESSION(Method.POST, "/rest/auth/1/session"),
    TEAM(Method.GET, "/rest/tempo-teams/1/team"),
    USER(Method.GET, "/rest/api/2/user"),
    TIMESHEET_APPROVAL(Method.GET, "/rest/tempo-timesheets/3/timesheet-approval");
    
    private final Method method;
    private final String path;
    
    private Command(Method method, String path) {
        this.method = method;
        this.path = path;
    }
    
    public Method getMethod() {
        return method;
    }
    
    public String getPath() {
        return path;
    }
}
