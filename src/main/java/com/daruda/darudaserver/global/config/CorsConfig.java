package com.daruda.darudaserver.global.config;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CorsConfig {
    public static CorsConfigurationSource configurationSource(){
        CorsConfiguration configuration = new CorsConfiguration();

        ArrayList<String> allowedOriginPatters = new ArrayList<>();
        allowedOriginPatters.add("http://localhost:5173");
        allowedOriginPatters.add("https://www.daruda.site");
        allowedOriginPatters.add("http://localhost:8080");
        configuration.setAllowedOrigins(allowedOriginPatters);

        ArrayList<String> allowedHttpMethods = new ArrayList<>();
        allowedHttpMethods.add("GET");
        allowedHttpMethods.add("POST");
        allowedHttpMethods.add("PUT");
        allowedHttpMethods.add("DELETE");
        allowedHttpMethods.add("OPTIONS");
        configuration.setAllowedMethods(allowedHttpMethods);

        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }


}
