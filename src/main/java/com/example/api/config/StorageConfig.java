package com.example.api.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "storage")
@Getter
@Setter
public class StorageConfig {
    private String bucket;
    private String cloudfrontEndpoint;

    /**
     * Returns the full Cloudfront URL for a material path
     *
     * @param materialPath the relative path of the material
     * @return the full Cloudfront URL
     */
    public String getFullMaterialUrl(String materialPath) {
        if (materialPath == null || materialPath.isEmpty()) {
            return null;
        }

        return String.format("%s/%s", cloudfrontEndpoint, materialPath);
    }
}
