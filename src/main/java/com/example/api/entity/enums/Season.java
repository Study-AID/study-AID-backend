package com.example.api.entity.enums;

public enum Season {
    spring, summer, fall, winter;

    public static Season fromString(String season) {
        for (Season s : Season.values()) {
            if (s.name().equalsIgnoreCase(season)) {
                return s;
            }
        }
        throw new IllegalArgumentException("Invalid season: " + season);
    }
}
