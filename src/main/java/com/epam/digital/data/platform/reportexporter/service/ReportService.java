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

import static com.epam.digital.data.platform.reportexporter.util.QueryFormatter.formatQueryList;
import static com.epam.digital.data.platform.reportexporter.util.ResponseHandler.handleResponse;
import static java.util.stream.Collectors.toList;

import com.epam.digital.data.platform.reportexporter.client.DashboardClient;
import com.epam.digital.data.platform.reportexporter.model.Dashboard;
import java.util.List;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;

@Service
public class ReportService {

  private final DashboardClient dashboardClient;

  private final DashboardArchiver archiver;
  private final QueryHelper queryHelper;

  public ReportService(DashboardClient client,
      QueryHelper queryHelper,
      DashboardArchiver archiver) {
    this.dashboardClient = client;
    this.archiver = archiver;
    this.queryHelper = queryHelper;
  }

  public List<Dashboard> getDashboards() {
    var dashboards =  handleResponse(dashboardClient.getDashboards()).getResults();

    return dashboards.stream()
        .filter(dashboard -> !dashboard.isDraft())
        .collect(toList());
  }

  public ByteArrayResource getArchive(String slug) {
    var dashboard = handleResponse(dashboardClient.getDashboardBySlug(slug));
    var queries = queryHelper.getUtilQueries(dashboard);

    return archiver.zipDashboard(slug, formatQueryList(queries), dashboard);
  }

}
