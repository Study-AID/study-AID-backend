package com.example.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@ConfigurationProperties(prefix = "storage")
@Getter
@Setter
public class StorageProperties {
    private String bucket;
}
