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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * Endpoints excluded from authentication. Overridden by {@code platform.security.whitelist};
 * the defaults are kept identical to the ones ddm-starter-security shipped.
 */
@Component
public class Whitelist {

  static final String[] DEFAULT_AUTH_WHITELIST = {
      "/swagger", "/v3/api-docs/**", "/swagger-ui/**", "/actuator/**"
  };

  private final RequestMatcher requestMatcher;

  public Whitelist(SecurityProperties properties) {
    List<RequestMatcher> matchers = CollectionUtils.isEmpty(properties.getWhitelist())
            ? Arrays.stream(DEFAULT_AUTH_WHITELIST)
                .map(AntPathRequestMatcher::new)
                .collect(Collectors.toList())
            : properties.getWhitelist().stream()
                .map(AntPathRequestMatcher::new)
                .collect(Collectors.toList());
    this.requestMatcher = new OrRequestMatcher(matchers);
  }

  public RequestMatcher getRequestMatcher() {
    return requestMatcher;
  }
}
