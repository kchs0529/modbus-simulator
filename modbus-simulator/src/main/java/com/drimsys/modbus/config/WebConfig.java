package com.drimsys.modbus.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * React 개발 서버(localhost:3000)에서 Spring Boot(localhost:8080)로
 * API 요청이 가능하도록 CORS를 허용한다.
 *
 * 프로덕션 배포 시에는 allowedOrigins를 실제 도메인으로 교체하거나
 * React 빌드 결과물을 Spring Boot static 폴더에 넣어서 동일 오리진으로 서빙한다.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(
                    "http://localhost:3000",  // React 개발 서버
                    "http://localhost:5173"   // Vite 개발 서버 (선택 시)
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(false);
    }
}
