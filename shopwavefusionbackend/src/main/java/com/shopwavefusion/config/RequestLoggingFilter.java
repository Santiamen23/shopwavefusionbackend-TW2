package com.shopwavefusion.config;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

	private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		long start = System.currentTimeMillis();
		String method = request.getMethod();
		String uri = request.getRequestURI();
		String query = request.getQueryString();
		String full = (query == null) ? uri : uri + "?" + query;

		String origin = request.getHeader("Origin");
		log.info("--> {} {} origin={}", method, full, origin);

		try {
			filterChain.doFilter(request, response);
		} finally {
			long duration = System.currentTimeMillis() - start;
			int status = response.getStatus();
			if (status >= 500) {
				log.error("<-- {} {} status={} duration={}ms", method, full, status, duration);
			} else if (status >= 400) {
				log.warn("<-- {} {} status={} duration={}ms", method, full, status, duration);
			} else {
				log.info("<-- {} {} status={} duration={}ms", method, full, status, duration);
			}
		}
	}
}