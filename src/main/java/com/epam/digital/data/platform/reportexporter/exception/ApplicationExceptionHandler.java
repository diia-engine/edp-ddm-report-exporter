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

package com.epam.digital.data.platform.reportexporter.exception;

import static com.epam.digital.data.platform.reportexporter.util.Header.TRACE_ID;

import com.epam.digital.data.platform.reportexporter.model.exception.DetailedErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class ApplicationExceptionHandler extends ResponseEntityExceptionHandler {

  private final Logger log = LoggerFactory.getLogger(ApplicationExceptionHandler.class);

  private final String RUNTIME_ERROR = "RUNTIME_ERROR";
  private final String NOT_FOUND = "NOT_FOUND";
  private final String DASHBOARD_ZIP_ERROR = "DASHBOARD_ZIP_ERROR";
  private final String FORBIDDEN_OPERATION = "FORBIDDEN_OPERATION";

  @ExceptionHandler(Exception.class)
  public ResponseEntity<DetailedErrorResponse<Void>> handleException(Exception exception) {
    log.error("Runtime error occurred", exception);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(newDetailedResponse(RUNTIME_ERROR));
  }

  @ExceptionHandler(DashboardZippingException.class)
  public ResponseEntity<DetailedErrorResponse<Void>> handleDashboardZippingException(
      DashboardZippingException exception) {
    log.error("Error during zipping dashboard with queries", exception);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(newDetailedResponse(DASHBOARD_ZIP_ERROR));
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<DetailedErrorResponse<Void>> handleAccessDeniedException(
      AccessDeniedException exception) {
    log.error("Access denied", exception);
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
        .body(newDetailedResponse(FORBIDDEN_OPERATION));
  }

  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<DetailedErrorResponse<Void>> handleNotFoundException(
      NotFoundException exception) {
    log.error("Redash object not found", exception);
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(newDetailedResponse(NOT_FOUND));
  }

  private <T> DetailedErrorResponse<T> newDetailedResponse(String code) {
    var response = new DetailedErrorResponse<T>();
    response.setTraceId(MDC.get(TRACE_ID.getHeaderName()));
    response.setCode(code);
    return response;
  }
}
