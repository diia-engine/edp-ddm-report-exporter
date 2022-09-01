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

package com.epam.digital.data.platform.reportexporter.controller;

import com.epam.digital.data.platform.reportexporter.model.Dashboard;
import java.util.List;
import com.epam.digital.data.platform.reportexporter.service.ReportService;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reports")
public class ReportController {

  private static final String CONTENT_DISPOSITION_HEADER_NAME = "Content-Disposition";
  private static final String ATTACHMENT_HEADER_VALUE = "attachment; filename=\"dashboard_%s.zip\"";

  private final ReportService service;

  public ReportController(
      ReportService service) {
    this.service = service;
  }

  @GetMapping
  public ResponseEntity<List<Dashboard>> getAllDashboards() {
    return ResponseEntity.ok().body(service.getDashboards());
  }

  @GetMapping(path = "/{slug_id}")
  public ResponseEntity<Resource> getDashboardArchive(@PathVariable("slug_id") String slugId) {
    var zip = service.getArchive(slugId);

    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_OCTET_STREAM)
        .contentLength(zip.getByteArray().length)
        .header(CONTENT_DISPOSITION_HEADER_NAME, String.format(ATTACHMENT_HEADER_VALUE, slugId))
        .body(zip);
  }
}
