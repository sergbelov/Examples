package ru.examples.jiraExample.utils.jira.api.json;

public class SimpleTeam extends Team {
    
    private String lead;
    private String mission;
    private String summary;

    /**
     * @return the lead
     */
    public String getLead() {
        return lead;
    }

    /**
     * @param lead the lead to set
     */
    public void setLead(String lead) {
        this.lead = lead;
    }

    /**
     * @return the mission
     */
    public String getMission() {
        return mission;
    }

    /**
     * @param mission the mission to set
     */
    public void setMission(String mission) {
        this.mission = mission;
    }

    /**
     * @return the summary
     */
    public String getSummary() {
        return summary;
    }

    /**
     * @param summary the summary to set
     */
    public void setSummary(String summary) {
        this.summary = summary;
    }
    
    @Override
    public String toString() {
        return "SimpleTeam{" + "lead=" + lead + ", mission=" + mission + ", summary=" + summary + ", " + super.toString() + '}';
    }
}
