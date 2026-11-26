package com.coworkhub.config;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class ConfiguracionBeans {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    Clock reloj(@Value("${coworkhub.reloj.fijo:}") String relojFijo) {
        if (relojFijo == null || relojFijo.isBlank()) {
            return Clock.systemDefaultZone();
        }
        ZoneId zona = ZoneId.systemDefault();
        return Clock.fixed(LocalDateTime.parse(relojFijo).atZone(zona).toInstant(), zona);
    }
}
