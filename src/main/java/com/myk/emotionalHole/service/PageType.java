package com.myk.emotionalHole.service;

public enum PageType {
    
    NEW("最新页", 0.1),
    
    RECOMMEND("推荐页", 0.2),
    
    HOT("热榜页", 0.3);

    private final String description;
    private final double atmosphereFactor;

    PageType(String description, double atmosphereFactor) {
        this.description = description;
        this.atmosphereFactor = atmosphereFactor;
    }

    public String getDescription() {
        return description;
    }

    public double getAtmosphereFactor() {
        return atmosphereFactor;
    }

    public static PageType fromString(String name) {
        for (PageType type : PageType.values()) {
            if (type.name().equalsIgnoreCase(name)) {
                return type;
            }
        }
        return NEW;
    }
}