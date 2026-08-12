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

import static java.util.List.of;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.ResultMatcher.matchAll;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.digital.data.platform.reportexporter.config.MapperConfig;
import com.epam.digital.data.platform.reportexporter.config.WebSecurityConfig;
import com.epam.digital.data.platform.reportexporter.controller.ReportController;
import com.epam.digital.data.platform.reportexporter.service.ReportService;
import com.epam.digital.data.platform.reportexporter.util.Header;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Covers the in-house replacement of ddm-starter-security: the JWT filter chain must reject
 * requests without an access token and accept requests carrying one.
 */
@WebMvcTest(ReportController.class)
@Import({
    WebSecurityConfig.class,
    JwtConfigurer.class,
    JwtAuthenticationFilter.class,
    TokenProvider.class,
    TokenParser.class,
    Whitelist.class,
    DefaultAuthenticationEntryPoint.class,
    DefaultAccessDeniedHandler.class,
    MapperConfig.class
})
@TestPropertySource(properties = {"platform.security.enabled=true"})
class JwtSecurityTest {

  private static final String BASE_URL = "/reports";

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private ReportService reportService;

  @Test
  void shouldReturn401WhenNoAccessToken() throws Exception {
    mockMvc.perform(get(BASE_URL))
        .andExpect(matchAll(
            status().isUnauthorized(),
            // the shared ObjectMapper is SNAKE_CASE, hence trace_id
            jsonPath("$.error.code").value(is("401")),
            jsonPath("$.error.message").value(is("Unauthorized"))));
  }

  @Test
  void shouldPassThroughWhenAccessTokenPresent() throws Exception {
    mockMvc.perform(get(BASE_URL).header(Header.ACCESS_TOKEN.getHeaderName(), signedToken()))
        .andExpect(status().isOk());
  }

  /**
   * The signature is never verified by the filter chain - as in the original starter - so any
   * syntactically valid JWS is enough here.
   */
  private String signedToken() throws Exception {
    var claims = new JWTClaimsSet.Builder()
        .claim("preferred_username", "test_user")
        .claim("realm_access", Map.of("roles", of("report_exporter")))
        .build();
    var jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
    jwt.sign(new MACSigner(new byte[32]));
    return jwt.serialize();
  }
}
