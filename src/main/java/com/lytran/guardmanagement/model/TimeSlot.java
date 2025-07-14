package com.lytran.guardmanagement.model;

public enum TimeSlot {
    MORNING("07:30 - 11:30"),
    AFTERNOON("11:30 - 15:30"),
    EVENING("15:30 - 19:30");

    private final String label;

    TimeSlot(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
