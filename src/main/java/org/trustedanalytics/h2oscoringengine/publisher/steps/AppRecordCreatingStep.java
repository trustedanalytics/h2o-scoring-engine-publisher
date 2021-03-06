/**
 * Copyright (c) 2015 Intel Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.trustedanalytics.h2oscoringengine.publisher.steps;


import static org.trustedanalytics.h2oscoringengine.publisher.http.CloudFoundryEndpoints.APPS_ENDPOINT;
import static org.trustedanalytics.h2oscoringengine.publisher.http.CloudFoundryResponsesJsonPaths.APP_GUID_JSON_PATH;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.trustedanalytics.h2oscoringengine.publisher.EnginePublicationException;
import org.trustedanalytics.h2oscoringengine.publisher.http.HttpCommunication;
import org.trustedanalytics.h2oscoringengine.publisher.http.JsonDataFetcher;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class AppRecordCreatingStep {

  private static final Logger LOGGER = LoggerFactory.getLogger(AppRecordCreatingStep.class);

  private final RestTemplate cfRestTemplate;
  private final String cfApiUrl;
  private final String cfAppsUrl;

  public AppRecordCreatingStep(String cfApiUrl, RestTemplate cfRestTemplate) {
    this.cfRestTemplate = cfRestTemplate;
    this.cfApiUrl = cfApiUrl;
    this.cfAppsUrl = cfApiUrl + APPS_ENDPOINT;
  }

  public AppRouteCreatingStep createAppRecord(String spaceGuid, String appName)
      throws EnginePublicationException {

    LOGGER.info("Creating app record for " + appName + " in space " + spaceGuid);
    String requestBody = createAppRecordBody(spaceGuid, appName);

    ResponseEntity<String> response = cfRestTemplate.exchange(cfAppsUrl, HttpMethod.POST,
        HttpCommunication.postRequest(requestBody), String.class);

    try {
      String appGuid = JsonDataFetcher.getStringValue(response.getBody(), APP_GUID_JSON_PATH);
      return new AppRouteCreatingStep(cfRestTemplate, cfApiUrl, appGuid);
    } catch (IOException e) {
      throw new EnginePublicationException("Unable to create CloudFoundry app record:", e);
    }
  }

  private String createAppRecordBody(String spaceGuid, String appName) {
    ObjectMapper mapper = new ObjectMapper();

    ObjectNode requestBody = mapper.createObjectNode();
    requestBody.put("name", appName);
    requestBody.put("space_guid", spaceGuid);

    return requestBody.toString();
  }
}
