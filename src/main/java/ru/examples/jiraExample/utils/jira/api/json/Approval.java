package ru.examples.jiraExample.utils.jira.api.json;

public class Approval {
    
    private Period period;
    //Необходимо отработать
    private int requiredSeconds; // 144000
    private int requiredSecondsRelativeToday; // 115200
    private String smartDateString; // "Current week"
    private String status; //open
    private int submittedSeconds; //0
    private SimpleUser user;
    //Отработанное время
    private int workedSeconds; //28800
    private Worklogs worklogs;

    /**
     * @return the period
     */
    public Period getPeriod() {
        return period;
    }

    /**
     * @param period the period to set
     */
    public void setPeriod(Period period) {
        this.period = period;
    }

    /**
     * @return the requiredSeconds
     */
    public int getRequiredSeconds() {
        return requiredSeconds;
    }

    /**
     * @param requiredSeconds the requiredSeconds to set
     */
    public void setRequiredSeconds(int requiredSeconds) {
        this.requiredSeconds = requiredSeconds;
    }

    /**
     * @return the requiredSecondsRelativeToday
     */
    public int getRequiredSecondsRelativeToday() {
        return requiredSecondsRelativeToday;
    }

    /**
     * @param requiredSecondsRelativeToday the requiredSecondsRelativeToday to set
     */
    public void setRequiredSecondsRelativeToday(int requiredSecondsRelativeToday) {
        this.requiredSecondsRelativeToday = requiredSecondsRelativeToday;
    }

    /**
     * @return the smartDateString
     */
    public String getSmartDateString() {
        return smartDateString;
    }

    /**
     * @param smartDateString the smartDateString to set
     */
    public void setSmartDateString(String smartDateString) {
        this.smartDateString = smartDateString;
    }

    /**
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * @return the submittedSeconds
     */
    public int getSubmittedSeconds() {
        return submittedSeconds;
    }

    /**
     * @param submittedSeconds the submittedSeconds to set
     */
    public void setSubmittedSeconds(int submittedSeconds) {
        this.submittedSeconds = submittedSeconds;
    }

    /**
     * @return the user
     */
    public SimpleUser getUser() {
        return user;
    }

    /**
     * @param user the user to set
     */
    public void setUser(SimpleUser user) {
        this.user = user;
    }

    /**
     * @return the workedSeconds
     */
    public int getWorkedSeconds() {
        return workedSeconds;
    }

    /**
     * @param workedSeconds the workedSeconds to set
     */
    public void setWorkedSeconds(int workedSeconds) {
        this.workedSeconds = workedSeconds;
    }

    /**
     * @return the worklogs
     */
    public Worklogs getWorklogs() {
        return worklogs;
    }

    /**
     * @param worklogs the worklogs to set
     */
    public void setWorklogs(Worklogs worklogs) {
        this.worklogs = worklogs;
    }
    
    @Override
    public String toString() {
        return "Approval{" + "period=" + period + ", requiredSeconds=" + requiredSeconds + ", requiredSecondsRelativeToday=" + requiredSecondsRelativeToday + ", smartDateString=" + smartDateString + ", status=" + status + ", submittedSeconds=" + submittedSeconds + ", user=" + user + ", workedSeconds=" + workedSeconds + ", worklogs=" + worklogs + '}';
    }
    
}
