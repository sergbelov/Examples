package ru.examples.jiraExample.utils.jira.api.json;

import java.net.URL;

public class Worklogs {
    
    private URL href;

    /**
     * @return the href
     */
    public URL getHref() {
        return href;
    }

    /**
     * @param href the href to set
     */
    public void setHref(URL href) {
        this.href = href;
    }
    
    @Override
    public String toString() {
        return "Worklogs{" + "href=" + href + '}';
    }
}
