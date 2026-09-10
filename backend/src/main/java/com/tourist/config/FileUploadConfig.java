package com.tourist.config;

import org.springframework.context.annotation.Configuration;

/**
 * File upload configuration.
 * See application.yml for file upload settings:
 *   - file.upload.path: ./uploads
 *   - spring.servlet.multipart.max-file-size: 50MB
 *   - spring.servlet.multipart.max-request-size: 50MB
 *
 * Static file serving is configured in WebMvcConfig via resource handler
 * mapping /uploads/** to file:./uploads/
 */
@Configuration
public class FileUploadConfig {

}
