package com.example.api.entity.enums;

public enum SummaryStatus {
    not_started, in_progress, completed;

    // "isEmpty" method to check if the enum is empty
    public boolean isEmpty() {
        return this == null || this.name().isEmpty();
    }
}
