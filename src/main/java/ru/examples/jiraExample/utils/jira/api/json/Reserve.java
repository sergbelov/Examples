package ru.examples.jiraExample.utils.jira.api.json;

import java.util.List;

public class Reserve {
    
    private int size;
    private List<Object> items;

    /**
     * @return the size
     */
    public int getSize() {
        return size;
    }

    /**
     * @param size the size to set
     */
    public void setSize(int size) {
        this.size = size;
    }

    /**
     * @return the items
     */
    public List<Object> getItems() {
        return items;
    }

    /**
     * @param items the items to set
     */
    public void setItems(List<Object> items) {
        this.items = items;
    }
    
    @Override
    public String toString() {
        return "Reserve{" + "size=" + size + ", items=" + items + '}';
    }
    
}
