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

import static com.epam.digital.data.platform.reportexporter.util.ResponseHandler.handleResponse;
import static java.util.stream.Collectors.toSet;

import com.epam.digital.data.platform.reportexporter.client.QueryClient;
import com.epam.digital.data.platform.reportexporter.model.Dashboard;
import com.epam.digital.data.platform.reportexporter.model.Query;
import com.epam.digital.data.platform.reportexporter.model.Widget;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;

@Component
public class QueryHelper {

  /**
   * Redash rejects GET /api/queries with {@code 400 Page size is out of range (1-250)} for
   * anything above this, so the whole query list has to be read page by page.
   */
  private static final int MAX_PAGE_SIZE = 250;
  private static final int FIRST_PAGE = 1;

  private final QueryClient queryClient;

  public QueryHelper(QueryClient queryClient) {
    this.queryClient = queryClient;
  }

  public Set<Query> getUtilQueries(Dashboard dashboard) {
    var queryIds = getUtilQueryIds(dashboard);

    return getAllQueries().stream()
        .filter(query -> queryIds.contains(query.getId()))
        .collect(toSet());
  }

  private Set<Integer> getUtilQueryIds(Dashboard dashboard) {
    return dashboard.getWidgets().stream()
        .filter(widget -> widget.getVisualization() != null)
        .flatMap(this::getParameterQueries)
        .collect(toSet());
  }

  /**
   * Reads every query of the Redash instance page by page. The first page already carries the
   * total count, so no extra probing request is needed.
   */
  private List<Query> getAllQueries() {
    var firstPage = handleResponse(queryClient.getQueries(MAX_PAGE_SIZE, FIRST_PAGE));
    var queries = new ArrayList<>(firstPage.getResults());

    var lastPage = (firstPage.getCount() + MAX_PAGE_SIZE - 1) / MAX_PAGE_SIZE;
    for (var page = FIRST_PAGE + 1; page <= lastPage; page++) {
      queries.addAll(handleResponse(queryClient.getQueries(MAX_PAGE_SIZE, page)).getResults());
    }

    return queries;
  }

  private Stream<Integer> getParameterQueries(Widget widget) {
    var options = widget.getVisualization().getQuery().getOptions();
    var queries = new HashSet<Integer>();

    var parameters = (List<Map<String, Object>>) options.get("parameters");

    for (var parameter : parameters) {
      if (parameter.get("type").equals("query")) {
        queries.add((Integer) parameter.get("queryId"));
      }
    }
    queries.add(widget.getVisualization().getQuery().getId());
    
    return queries.stream();
  }
}
