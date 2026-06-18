package com.shopwavefusion.config;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
public class ProjectSecurityConfig {

	private static final Logger log = LoggerFactory.getLogger(ProjectSecurityConfig.class);

	@Value("${CORS_ALLOWED_ORIGINS:*}")
	private String corsAllowedOrigins;

	@Bean
	SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {

		List<String> origins = Arrays.stream(corsAllowedOrigins.split(","))
				.map(String::trim)
				.filter(s -> !s.isEmpty())
				.collect(Collectors.toList());

		log.info("CORS allowed origins loaded: {}", origins);

		http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.cors(corsCustomizer -> corsCustomizer.configurationSource(request -> {
					CorsConfiguration config = new CorsConfiguration();
					config.setAllowedOriginPatterns(origins);
					config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
					config.setAllowCredentials(true);
					config.setAllowedHeaders(List.of("*"));
					config.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));
					config.setMaxAge(3600L);
					return config;
				})).csrf((csrf) -> csrf.disable())
				.addFilterAfter(new JWTTokenGeneratorFilter(), BasicAuthenticationFilter.class)
				.addFilterBefore(new JWTTokenValidatorFilter(), BasicAuthenticationFilter.class)
				.exceptionHandling(exceptions -> exceptions
						.authenticationEntryPoint((request, response, authException) -> response
								.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.getMessage())))
				.authorizeHttpRequests((requests) -> requests
						.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
						.requestMatchers("/auth/signin").authenticated()
						.requestMatchers("/auth/signup").permitAll()
						.requestMatchers(HttpMethod.GET, "/ratings/**", "/reviews/**").permitAll()
						.requestMatchers("/admin/products/**", "/admin/orders/**", "/admin/control/**")
							.hasRole("ADMIN")
						.requestMatchers("/cart/**", "/users/**", "/cart_items/**", "/orders/**", "/ratings/**",
								"/reviews/**")
							.hasAnyRole("USER", "ADMIN")
						.requestMatchers("/products/**", "/all", "/", "/swagger-ui/**", "/v3/api-docs/**")
							.permitAll()
						.anyRequest().permitAll())
				.httpBasic(Customizer.withDefaults());
		return http.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

}

