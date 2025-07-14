package com.lytran.guardmanagement.model;

public enum Block {
    BLOCK_3("Block 3"),
    BLOCK_4("Block 4"),
    BLOCK_5("Block 5"),
    BLOCK_6("Block 6"),
    BLOCK_8("Block 8"),
    BLOCK_10("Block 10"),
    BLOCK_11("Block 11");

    private final String label;

    Block(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
