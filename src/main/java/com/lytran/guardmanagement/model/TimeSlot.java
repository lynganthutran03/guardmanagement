package com.lytran.guardmanagement.model;

public enum TimeSlot {
    DAY_SHIFT("07:30 - 14:30"),
    NIGHT_SHIFT("14:30 - 21:30");

    private final String label;

    TimeSlot(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
