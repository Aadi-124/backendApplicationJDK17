package com.example.BackendApplication.Security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {
    
    @Autowired
    public MongoDBUserDetailsService userDetailsService;


    @SuppressWarnings("deprecation")
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        
        http.csrf(csrf -> csrf.disable());
        // http.authorizeHttpRequests(auth->auth.anyRequest().permitAll());
            // http
            //     .authorizeHttpRequests(auth ->{
            //     auth.requestMatchers("/public/**","/users/**").permitAll();
            //     auth.requestMatchers("/admin/**").hasRole("ADMIN");
            // });
            http
            .authorizeRequests(authz -> authz
                .requestMatchers("/public/**").permitAll()
                .requestMatchers(adminMatchers()).hasRole("ADMIN") // All admin URLs require ADMIN role
                .requestMatchers(userMatchers()).hasAnyRole("ADMIN", "USER") // All user URLs for both ADMIN and USER
                .anyRequest().authenticated());
         http.httpBasic(Customizer.withDefaults());
        return http.build();
    }                       

    private RequestMatcher adminMatchers() {
        return new AntPathRequestMatcher("/admin/**");
    }

    private RequestMatcher userMatchers() {
        return new AntPathRequestMatcher("/user/**");
    }


    @Bean
    public AuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public UserDetailsService userDetailsService(){
        return userDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
   

}
