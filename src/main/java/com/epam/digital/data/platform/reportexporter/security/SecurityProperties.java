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

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Replacement for {@code com.epam.digital.data.platform.starter.security.config.SecurityProperties}
 * from ddm-starter-security, which is no longer available to this service.
 */
@ConfigurationProperties(prefix = "platform.security")
public class SecurityProperties {

  private List<String> whitelist;

  public List<String> getWhitelist() {
    return whitelist;
  }

  public void setWhitelist(List<String> whitelist) {
    this.whitelist = whitelist;
  }
}
