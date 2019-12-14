package ru.examples.jiraExample.utils.jira.api.json;

import java.util.Date;

public class Period {
    
    private Date dateFrom;
    private Date dateTo;
    private String periodView;
    private String periodId;

    /**
     * @return the dateFrom
     */
    public Date getDateFrom() {
        return dateFrom;
    }

    /**
     * @param dateFrom the dateFrom to set
     */
    public void setDateFrom(Date dateFrom) {
        this.dateFrom = dateFrom;
    }

    /**
     * @return the dateTo
     */
    public Date getDateTo() {
        return dateTo;
    }

    /**
     * @param dateTo the dateTo to set
     */
    public void setDateTo(Date dateTo) {
        this.dateTo = dateTo;
    }

    /**
     * @return the periodView
     */
    public String getPeriodView() {
        return periodView;
    }

    /**
     * @param periodView the periodView to set
     */
    public void setPeriodView(String periodView) {
        this.periodView = periodView;
    }

    /**
     * @return the periodId
     */
    public String getPeriodId() {
        return periodId;
    }

    /**
     * @param periodId the periodId to set
     */
    public void setPeriodId(String periodId) {
        this.periodId = periodId;
    }
    
     @Override
    public String toString() {
        return "Period{" + "dateFrom=" + dateFrom + ", dateTo=" + dateTo + ", periodView=" + periodView + ", periodId=" + periodId + '}';
    }
    
}
