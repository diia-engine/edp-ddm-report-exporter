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

import com.epam.digital.data.platform.reportexporter.exception.JwtParsingException;
import com.epam.digital.data.platform.reportexporter.model.security.JwtClaimsDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jwt.SignedJWT;
import java.text.ParseException;
import org.springframework.stereotype.Component;

/**
 * Reads the claims out of an access token.
 *
 * <p>The signature is deliberately <b>not</b> verified here - exactly as ddm-starter-security
 * behaved. Signature validation is performed upstream by the platform gateway, and this service
 * must not become stricter than the rest of the platform without a coordinated change.
 */
@Component
public class TokenParser {

  private final ObjectMapper objectMapper;

  public TokenParser(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  public JwtClaimsDto parseClaims(String token) {
    try {
      var signedJwt = SignedJWT.parse(token);
      return objectMapper.readValue(signedJwt.getPayload().toString(), JwtClaimsDto.class);
    } catch (ParseException | JsonProcessingException e) {
      throw new JwtParsingException(e.getMessage());
    }
  }
}
