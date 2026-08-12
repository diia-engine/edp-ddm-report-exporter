/*
 * Copyright 2021 EPAM Systems.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.digital.data.platform.reportexporter.config;

import com.epam.digital.data.platform.reportexporter.security.JwtConfigurer;
import com.epam.digital.data.platform.reportexporter.security.SecurityProperties;
import com.epam.digital.data.platform.reportexporter.security.Whitelist;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRepository;

/**
 * In-house replacement for the security configuration ddm-starter-security used to auto-configure.
 * Behaviour is kept identical: stateless JWT authentication for every request except the
 * whitelisted ones.
 */
@Order(WebSecurityConfig.DEFAULT_ORDER)
@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(SecurityProperties.class)
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
@ConditionalOnProperty(prefix = "platform.security", name = "enabled", matchIfMissing = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

  public static final int DEFAULT_ORDER = Integer.MIN_VALUE + 10;

  private final JwtConfigurer securityConfigurerAdapter;
  private final AccessDeniedHandler accessDeniedHandler;
  private final AuthenticationEntryPoint authenticationErrorHandler;
  private final Whitelist whitelist;
  private final boolean csrfEnabled;

  public WebSecurityConfig(
      JwtConfigurer securityConfigurerAdapter,
      AccessDeniedHandler accessDeniedHandler,
      AuthenticationEntryPoint authenticationErrorHandler,
      Whitelist whitelist,
      @Value("${platform.security.csrf.enabled:false}") boolean csrfEnabled) {
    this.securityConfigurerAdapter = securityConfigurerAdapter;
    this.accessDeniedHandler = accessDeniedHandler;
    this.authenticationErrorHandler = authenticationErrorHandler;
    this.whitelist = whitelist;
    this.csrfEnabled = csrfEnabled;
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http = csrfEnabled ? http.csrf().csrfTokenRepository(tokenRepository()).and() : http.csrf().disable();
    http.exceptionHandling()
        .authenticationEntryPoint(authenticationErrorHandler)
        .accessDeniedHandler(accessDeniedHandler)
        .and()
        .sessionManagement()
        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        .and()
        .authorizeRequests()
        .anyRequest()
        .authenticated()
        .and()
        .apply(securityConfigurerAdapter);
  }

  @Override
  public void configure(WebSecurity web) {
    web.ignoring().requestMatchers(whitelist.getRequestMatcher());
  }

  @Bean
  public InMemoryUserDetailsManager inMemoryUserDetailsManager() {
    return new InMemoryUserDetailsManager();
  }

  @Bean
  public CsrfTokenRepository tokenRepository() {
    return CookieCsrfTokenRepository.withHttpOnlyFalse();
  }
}
