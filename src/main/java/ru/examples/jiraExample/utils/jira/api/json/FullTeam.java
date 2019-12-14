package ru.examples.jiraExample.utils.jira.api.json;

public class FullTeam extends SimpleTeam {

    private LeadUser leadUser;

    /**
     * @return the leadUser
     */
    public LeadUser getLeadUser() {
        return leadUser;
    }

    /**
     * @param leadUser the leadUser to set
     */
    public void setLeadUser(LeadUser leadUser) {
        this.leadUser = leadUser;
    }

    @Override
    public String toString() {
        return "FullTeam{" + "leadUser=" + leadUser + ", " + super.toString() + '}';
    }
}
