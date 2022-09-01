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

import io.fabric8.kubernetes.client.KubernetesClient;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;

@Component
public class OpenShiftClient {

  private final String SECRET_NAME = "redash-api-keys";
  private final String ADMIN_KEY = "admin-api-key";

  private final Supplier<KubernetesClient> kubernetesClientFactory;
  private final Map<String, String> apiKeyMap = new ConcurrentHashMap<>();

  public OpenShiftClient(Supplier<KubernetesClient> kubernetesClientFactory) {
    this.kubernetesClientFactory = kubernetesClientFactory;
  }

  public String getApiKey() {
    var encodedKey = getEncodedApiKey(ADMIN_KEY);

    return decodeKey(encodedKey);
  }

  private String decodeKey(String encodedKey) {
    return apiKeyMap.computeIfAbsent(encodedKey,
        key -> new String(Base64.getDecoder().decode(encodedKey), StandardCharsets.UTF_8));
  }

  private String getEncodedApiKey(String apiKey) {
    KubernetesClient kubernetesClient = null;

    try {
      kubernetesClient = kubernetesClientFactory.get();

      return kubernetesClient.secrets()
          .withName(SECRET_NAME)
          .get()
          .getData()
          .get(apiKey);
    } finally {
      if (kubernetesClient != null) {
        kubernetesClient.close();
      }
    }
  }
}