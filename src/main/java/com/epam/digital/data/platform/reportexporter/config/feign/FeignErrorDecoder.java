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

package com.epam.digital.data.platform.reportexporter.config.feign;

import com.epam.digital.data.platform.reportexporter.exception.NotFoundException;
import feign.Response;
import feign.RetryableException;
import feign.codec.ErrorDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

public class FeignErrorDecoder implements ErrorDecoder {

  private final ErrorDecoder defaultErrorDecoder = new Default();
  private final Logger log = LoggerFactory.getLogger(FeignErrorDecoder.class);

  @Override
  public Exception decode(String methodKey, Response response) {
    var status = HttpStatus.valueOf(response.status());
    var exception = defaultErrorDecoder.decode(methodKey, response);

    if (status.equals(HttpStatus.NOT_FOUND)) {
      throw new NotFoundException("Redash object not found");
    }

    if (exception instanceof RetryableException) {
      return exception;
    }

    log.error("Error during call to Redash, status code {}, response body: {}",
        response.status(), response.body());

    if (status.is5xxServerError()) {
      return new RetryableException(status.value(), "Redash server error",
          response.request().httpMethod(), null, response.request());
    }

    return exception;
  }
}