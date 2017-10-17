package com.ycb.app.provider;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Created by zhuhui on 17-6-16.
 */
@SpringBootApplication
@EnableDiscoveryClient
public class AppProviderApplication {
    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(AppProviderApplication.class);
        application.run(args);
    }
}
