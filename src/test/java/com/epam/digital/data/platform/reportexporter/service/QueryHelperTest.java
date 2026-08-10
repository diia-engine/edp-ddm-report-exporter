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

package com.epam.digital.data.platform.reportexporter.service;

import com.epam.digital.data.platform.reportexporter.client.QueryClient;
import com.epam.digital.data.platform.reportexporter.model.Dashboard;
import com.epam.digital.data.platform.reportexporter.model.Page;
import com.epam.digital.data.platform.reportexporter.model.Query;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ResourceUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class QueryHelperTest {

  /** Hard limit enforced by Redash on GET /api/queries?page_size=... */
  static final int REDASH_MAX_PAGE_SIZE = 250;

  QueryHelper instance;

  ObjectMapper objectMapper = new ObjectMapper();

  Dashboard dashboard;

  @Mock
  QueryClient queryClient;

  @BeforeEach
  void setup() throws IOException {
    instance = new QueryHelper(queryClient);
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    dashboard = objectMapper
        .readValue(ResourceUtils.getFile("classpath:dashboards/dashboard.json"), Dashboard.class);
  }

  @Test
  void shouldReturnNotEmptySetWhenFoundParameterQueries() {
    var response = mockPageResponse(HttpStatus.OK, 5, generateQueries(1, 6));
    when(queryClient.getQueries(anyInt(), anyInt())).thenReturn(response);

    var result = instance.getUtilQueries(dashboard);

    verify(queryClient).getQueries(REDASH_MAX_PAGE_SIZE, 1);
    verifyNoMoreInteractions(queryClient);

    assertThat(result.size()).isEqualTo(4);
    assertThat(result)
        .containsExactlyInAnyOrder(generateQueries(1, 5).toArray(new Query[0]));
  }

  private ResponseEntity<Page<Query>> mockPageResponse(HttpStatus status, int count,
      List<Query> queries) {
    var page = new Page<Query>();
    page.setCount(count);
    page.setResults(queries);

    return new ResponseEntity<>(page, status);
  }

  private List<Query> generateQueries(int startIndex, int endIndex) {
    var queries = new ArrayList<Query>();

    IntStream.range(startIndex, endIndex).forEach(id -> {
      var query = new Query();
      query.setId(id);
      query.setName("Query #" + id);
      query.setQuery("Query-" + id);
      queries.add(query);
    });

    return queries;
  }

  /**
   * Guards the fix for the failure seen against a real Redash: asking for the whole query list in
   * one page produced {@code GET /api/queries?page_size=252 -> 400 Page size is out of range
   * (1-250)} and a 500 for the whole {@code GET /reports/{id}}.
   *
   * Page 1 here is full and holds nothing the dashboard references - the two queries it needs are
   * on page 2, so the result also proves the pages are merged rather than only the first one read.
   */
  @Test
  void shouldReadQueriesPageByPageWhenCountExceedsRedashPageSizeLimit() {
    var queryCount = 252;
    when(queryClient.getQueries(REDASH_MAX_PAGE_SIZE, 1))
        .thenReturn(mockPageResponse(HttpStatus.OK, queryCount, generateQueries(100, 350)));
    when(queryClient.getQueries(REDASH_MAX_PAGE_SIZE, 2))
        .thenReturn(mockPageResponse(HttpStatus.OK, queryCount, generateQueries(1, 3)));

    var result = instance.getUtilQueries(dashboard);

    var requestedPageSize = ArgumentCaptor.forClass(Integer.class);
    var requestedPage = ArgumentCaptor.forClass(Integer.class);
    verify(queryClient, times(2)).getQueries(requestedPageSize.capture(), requestedPage.capture());

    assertThat(requestedPageSize.getAllValues())
        .allSatisfy(pageSize -> assertThat(pageSize).isBetween(1, REDASH_MAX_PAGE_SIZE));
    assertThat(requestedPage.getAllValues()).containsExactly(1, 2);
    assertThat(result).containsExactlyInAnyOrder(generateQueries(1, 3).toArray(new Query[0]));
  }
}
