package ru.examples.jiraExample.utils.jira.api.json;

import java.util.List;

public class Timesheet {
    
    private List<Approval> approvals;
    private Period period;
    private Team team;

    /**
     * @return the approvals
     */
    public List<Approval> getApprovals() {
        return approvals;
    }

    /**
     * @param approvals the approvals to set
     */
    public void setApprovals(List<Approval> approvals) {
        this.approvals = approvals;
    }

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
     * @return the team
     */
    public Team getTeam() {
        return team;
    }

    /**
     * @param team the team to set
     */
    public void setTeam(Team team) {
        this.team = team;
    }
    
    @Override
    public String toString() {
        return "TimesheetApproval{" + "approvals=" + approvals + ", period=" + period + ", team=" + team + '}';
    }
    
}
