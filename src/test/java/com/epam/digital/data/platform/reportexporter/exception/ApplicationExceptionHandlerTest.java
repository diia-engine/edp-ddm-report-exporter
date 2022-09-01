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

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.ResultMatcher.matchAll;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@WebMvcTest
@ContextConfiguration(classes = {MockController.class, ApplicationExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = false)
class ApplicationExceptionHandlerTest extends ResponseEntityExceptionHandler {

  private static final String BASE_URL = "/mock";
  private static final String ENTITY_ID = "ENTITY_ID";

  private static final String FORBIDDEN_OPERATION = "FORBIDDEN_OPERATION";

  private static final String VALID_INPUT = "{\"recordId\":\"6d34cbd7-dded-495a-a1c6-4f37d823b59d\","
      + "\"excerptType\":\"validtype\",\"updatedAt\":\"2021-07-29T11:52:39.972053\"}";

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private ObjectMapper objectMapper;
  @MockBean
  private MockService mockService;

  @Test
  void shouldReturnInternalErrorWhenDashboardZipException() throws Exception {
    when(mockService.getArchive(any())).thenThrow(new DashboardZippingException("FAILED", new RuntimeException()));

    mockMvc.perform(get(BASE_URL + "/{id}", ENTITY_ID))
        .andExpect(status().isInternalServerError())
        .andExpect(response -> assertTrue(
            response.getResolvedException() instanceof DashboardZippingException))
        .andExpect(matchAll(
            jsonPath("$.code").value(is("DASHBOARD_ZIP_ERROR"))
        ));
  }

  @Test
  void shouldReturnRuntimeErrorOnGenericException() throws Exception {
    when(mockService.getArchive(any())).thenThrow(RuntimeException.class);

    mockMvc.perform(get(BASE_URL + "/{slug_id}", ENTITY_ID))
        .andExpect(status().isInternalServerError())
        .andExpect(matchAll(
            jsonPath("$.code").value(is("RUNTIME_ERROR")),
            jsonPath("$.statusDetails").doesNotExist()));
  }

  @Test
  void shouldReturn403WhenForbiddenOperation() throws Exception {
    when(mockService.getArchive(any())).thenThrow(AccessDeniedException.class);

    mockMvc
        .perform(get(BASE_URL + "/{slug_id}", ENTITY_ID))
        .andExpect(
            matchAll(
                status().isForbidden(),
                jsonPath("$.code").value(is(FORBIDDEN_OPERATION))));
  }

  @Test
  void shouldReturnNotFoundWhenSlugDoesNotExist() throws Exception {
    when(mockService.getArchive(any())).thenThrow(new NotFoundException("No slug found"));

    mockMvc.perform(get(BASE_URL + "/{id}", ENTITY_ID))
        .andExpect(status().isNotFound())
        .andExpect(response -> assertTrue(
            response.getResolvedException() instanceof NotFoundException))
        .andExpect(matchAll(
            jsonPath("$.code").value(is("NOT_FOUND"))
        ));
  }

}
