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

package com.epam.digital.data.platform.reportexporter.security;

import com.epam.digital.data.platform.reportexporter.util.Header;
import java.io.IOException;
import java.util.Objects;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/** Populates the security context from the {@code X-Access-Token} header. */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

  private final TokenProvider tokenProvider;
  private final Whitelist whitelist;

  public JwtAuthenticationFilter(TokenProvider tokenProvider, Whitelist whitelist) {
    this.tokenProvider = tokenProvider;
    this.whitelist = whitelist;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    var jwt = resolveToken(request);
    if (StringUtils.hasText(jwt)) {
      var authentication = tokenProvider.getAuthentication(jwt);
      SecurityContextHolder.getContext().setAuthentication(authentication);
    } else {
      log.warn("no valid JWT token found, uri: {}", request.getRequestURI());
    }
    filterChain.doFilter(request, response);
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    return whitelist.getRequestMatcher().matches(request);
  }

  private String resolveToken(HttpServletRequest request) {
    var token = request.getHeader(Header.ACCESS_TOKEN.getHeaderName());
    return Objects.isNull(token) ? "" : token;
  }
}
