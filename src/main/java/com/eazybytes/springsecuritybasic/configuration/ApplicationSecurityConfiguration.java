package com.eazybytes.springsecuritybasic.configuration;

import com.eazybytes.springsecuritybasic.configuration.impl.CustomUserDetailsImpl;
import com.eazybytes.springsecuritybasic.enumeration.AuthorityEnum;
import com.eazybytes.springsecuritybasic.model.Customer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;

@Configuration
public class ApplicationSecurityConfiguration extends WebSecurityConfigurerAdapter {
//    @Autowired
//    @Qualifier("inMemoryCustomUserDetailsManagerImpl")
//    private UserDetailsManager userDetailsManager;

    @Autowired
    @Qualifier("inJdbcCustomUserDetailsManagerImpl")
    private UserDetailsManager userDetailsManager;

    @Value( "${application.admin.username}" )
    private String adminUsername;

    @Value( "${application.admin.password}" )
    private String adminPassword;

    @Value("${application.allowCORS.urls}")
    private List<String> allowCORSUrls;

    @Value("${application.allowCORS.methods}")
    private List<String> allowCORSmMethods;

    private final String CUSTOMER_AUTHORITY = AuthorityEnum.CUSTOMER.getName();

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
        .cors().configurationSource(createCORSConfiguration())
        .and().csrf().csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
        .and().authorizeRequests()
            .antMatchers("/user").hasAuthority(CUSTOMER_AUTHORITY)
        .and()
        .formLogin()
        .and()
        .httpBasic();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        Customer customer = new Customer();
        customer.setEmail(this.adminUsername);
        customer.setPwd(this.adminPassword);
        customer.setRole(CUSTOMER_AUTHORITY);
        customer.setEnabled(true);

        this.userDetailsManager.createUser(new CustomUserDetailsImpl(customer));
        auth.userDetailsService(this.userDetailsManager);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    public CorsConfigurationSource createCORSConfiguration() {
        return new CorsConfigurationSource() {
            @Override
            public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
                CorsConfiguration config = new CorsConfiguration();
                config.setAllowedOrigins(allowCORSUrls);
                config.setAllowedMethods(allowCORSmMethods);
                config.setAllowedHeaders(Collections.singletonList("*"));
                config.setAllowCredentials(true);
                config.setMaxAge(3600L);

                return config;
            }
        };
    }
}
