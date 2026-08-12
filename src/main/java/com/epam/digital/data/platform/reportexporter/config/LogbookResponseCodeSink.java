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

import java.io.IOException;
import org.slf4j.MDC;
import org.zalando.logbook.Correlation;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.Precorrelation;
import org.zalando.logbook.Sink;

/**
 * Puts the HTTP status into MDC so that the {@code responseCode} field of log4j2-layout.json gets
 * populated. Ported from ddm-starter-logger2, which is no longer available to this service.
 */
public class LogbookResponseCodeSink implements Sink {

  private static final String RESPONSE_CODE_MDC_KEY = "responseCode";

  private final Sink delegate;

  public LogbookResponseCodeSink(Sink delegate) {
    this.delegate = delegate;
  }

  @Override
  public void write(Precorrelation precorrelation, HttpRequest request) throws IOException {
    delegate.write(precorrelation, request);
  }

  @Override
  public void write(Correlation correlation, HttpRequest request, HttpResponse response)
      throws IOException {
    MDC.put(RESPONSE_CODE_MDC_KEY, String.valueOf(response.getStatus()));
    delegate.write(correlation, request, response);
    MDC.remove(RESPONSE_CODE_MDC_KEY);
  }
}
