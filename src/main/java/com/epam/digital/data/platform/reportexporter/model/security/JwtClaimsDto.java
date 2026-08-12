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

package com.epam.digital.data.platform.reportexporter.model.security;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Only the claims this service actually consumes. ddm-starter-security exposed a much wider DTO
 * (edrpou, drfo, KATOTTG, subjectType and so on), but report-exporter never read those.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class JwtClaimsDto {

  @JsonProperty("preferred_username")
  private String preferredUsername;

  @JsonProperty("realm_access")
  private RolesDto realmAccess;

  public String getPreferredUsername() {
    return preferredUsername;
  }

  public void setPreferredUsername(String preferredUsername) {
    this.preferredUsername = preferredUsername;
  }

  public RolesDto getRealmAccess() {
    return realmAccess;
  }

  public void setRealmAccess(RolesDto realmAccess) {
    this.realmAccess = realmAccess;
  }
}
