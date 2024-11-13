package com.bkm009.video_editor.configs;

import com.bkm009.video_editor.auth.ApiKeyFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
public class AuthConfig  {

    @Autowired
    private ApiKeyFilter apiKeyFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .httpBasic().disable()
                .formLogin().disable()
                .logout().disable()
                .authorizeHttpRequests()
                .anyRequest().permitAll()
                .and()
                .addFilterBefore(apiKeyFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
