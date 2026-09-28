package com.example.framework.file.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
@EnableConfigurationProperties(FileProperties.class)
public class FileWebMvcConfig implements WebMvcConfigurer {

    private final FileProperties fileProperties;

    public FileWebMvcConfig(FileProperties fileProperties) {
        this.fileProperties = fileProperties;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String accessPrefix = fileProperties.getAccessPrefix();
        if (!accessPrefix.endsWith("/")) {
            accessPrefix += "/";
        }
        Path basePath = Paths.get(fileProperties.getBasePath()).toAbsolutePath().normalize();
        String baseLocation = basePath.toUri().toString();
        registry.addResourceHandler(accessPrefix + "**")
                .addResourceLocations(baseLocation);
    }
}
