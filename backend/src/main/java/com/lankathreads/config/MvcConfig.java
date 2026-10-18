package com.lankathreads.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        String baseDir = com.lankathreads.util.FileUploadUtil.getWebDir();

        // 1. Expose uploads directory (/uploads/**)
        File uploadsDir = new File(baseDir, "uploads");
        String uploadsUri = uploadsDir.toURI().toString();
        if (!uploadsUri.endsWith("/")) {
            uploadsUri += "/";
        }
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadsUri);

        // 2. Expose product uploads and legacy images under /images/**
        File productUploadsDir = new File(uploadsDir, "products");
        String productUploadsUri = productUploadsDir.toURI().toString();
        if (!productUploadsUri.endsWith("/")) {
            productUploadsUri += "/";
        }

        File frontendImagesDir = new File(baseDir, "../frontend/static/images");
        if (!frontendImagesDir.exists()) {
            frontendImagesDir = new File(baseDir, "frontend/static/images");
        }
        String frontendImagesUri = frontendImagesDir.toURI().toString();
        if (!frontendImagesUri.endsWith("/")) {
            frontendImagesUri += "/";
        }

        File targetImagesDir = new File(baseDir, "target/classes/static/images");
        String targetImagesUri = targetImagesDir.toURI().toString();
        if (!targetImagesUri.endsWith("/")) {
            targetImagesUri += "/";
        }

        // Map /images/products/** to uploads/products as well as static images
        registry.addResourceHandler("/images/products/**")
                .addResourceLocations(productUploadsUri, frontendImagesUri + "products/", targetImagesUri + "products/", "classpath:/static/images/products/");

        // Map general /images/**
        registry.addResourceHandler("/images/**")
                .addResourceLocations(frontendImagesUri, targetImagesUri, "classpath:/static/images/");
    }
}

