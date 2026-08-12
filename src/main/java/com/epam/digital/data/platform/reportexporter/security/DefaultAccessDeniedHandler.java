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

import static com.epam.digital.data.platform.reportexporter.util.Header.TRACE_ID;

import com.epam.digital.data.platform.reportexporter.model.exception.ErrorDto;
import com.epam.digital.data.platform.reportexporter.model.exception.ErrorRestResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
public class DefaultAccessDeniedHandler implements AccessDeniedHandler {

  private final ObjectMapper objectMapper;

  public DefaultAccessDeniedHandler(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException exception)
      throws IOException {
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setStatus(HttpStatus.FORBIDDEN.value());
    var error = new ErrorDto(
            String.valueOf(HttpStatus.FORBIDDEN.value()),
            exception.getMessage(),
            MDC.get(TRACE_ID.getHeaderName()));
    response.getWriter().write(objectMapper.writeValueAsString(new ErrorRestResponseDto(error)));
  }
}
