package com.prueba_cuscatlan.shopping_Car_miguel.config;

import org.h2.server.web.JakartaWebServlet;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(JakartaWebServlet.class)
@ConditionalOnProperty(name = "spring.h2.console.enabled", havingValue = "true")
public class H2ConsoleConfig {

    @Bean
    ServletRegistrationBean<JakartaWebServlet> h2Console() {
        return new ServletRegistrationBean<>(new JakartaWebServlet(), "/h2-console/*");
    }
}
