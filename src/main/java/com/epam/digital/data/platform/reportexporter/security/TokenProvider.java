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

import com.epam.digital.data.platform.reportexporter.model.security.JwtClaimsDto;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component
public class TokenProvider {

  private final Logger log = LoggerFactory.getLogger(TokenProvider.class);

  private final TokenParser tokenParser;

  public TokenProvider(TokenParser tokenParser) {
    this.tokenParser = tokenParser;
  }

  public Authentication getAuthentication(String token) {
    log.debug("Start parsing access token");
    var claims = tokenParser.parseClaims(token);
    log.debug("Access token decoded successfully");
    var authorities = getAuthorities(claims);
    var principal = new User(claims.getPreferredUsername(), "", authorities);
    return new UsernamePasswordAuthenticationToken(principal, token, authorities);
  }

  /** Realm roles are mapped as-is, without the {@code ROLE_} prefix. */
  private Collection<GrantedAuthority> getAuthorities(JwtClaimsDto claims) {
    if (Objects.isNull(claims.getRealmAccess())
        || CollectionUtils.isEmpty(claims.getRealmAccess().getRoles())) {
      return Collections.emptyList();
    }
    return claims.getRealmAccess().getRoles().stream()
        .map(SimpleGrantedAuthority::new)
        .collect(Collectors.toList());
  }
}
