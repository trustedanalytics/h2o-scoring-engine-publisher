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

import static org.trustedanalytics.h2oscoringengine.publisher.http.CloudFoundryEndpoints.BIND_ROUTE_TO_APP_ENDPOINT_TEMPLATE;
import static org.trustedanalytics.h2oscoringengine.publisher.http.CloudFoundryEndpoints.GET_ROUTES_ENDPOINT_TEMPLATE;
import static org.trustedanalytics.h2oscoringengine.publisher.http.CloudFoundryEndpoints.ROUTES_ENDPOINT;
import static org.trustedanalytics.h2oscoringengine.publisher.http.CloudFoundryEndpoints.SHARED_DOMAINS_ENDPOINT;
import static org.trustedanalytics.h2oscoringengine.publisher.http.CloudFoundryResponsesJsonPaths.DOMAIN_JSON_PATH;
import static org.trustedanalytics.h2oscoringengine.publisher.http.CloudFoundryResponsesJsonPaths.ROUTES_NUMBER_JSON_PATH;
import static org.trustedanalytics.h2oscoringengine.publisher.http.CloudFoundryResponsesJsonPaths.ROUTE_GUID_JSON_PATH;
import static org.trustedanalytics.h2oscoringengine.publisher.http.CloudFoundryResponsesJsonPaths.ROUTE_JSON_PATH;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.trustedanalytics.h2oscoringengine.publisher.EnginePublicationException;
import org.trustedanalytics.h2oscoringengine.publisher.http.HttpCommunication;
import org.trustedanalytics.h2oscoringengine.publisher.http.JsonDataFetcher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class AppRouteCreatingStep {

  private static final Logger LOGGER = LoggerFactory.getLogger(AppRouteCreatingStep.class);

  private final RestTemplate cfRestTemplate;
  private final String cfApiUrl;
  private final String appGuid;

  public AppRouteCreatingStep(RestTemplate cfRestTemplate, String cfApiUrl, String appGuid) {
    this.cfRestTemplate = cfRestTemplate;
    this.cfApiUrl = cfApiUrl;
    this.appGuid = appGuid;
  }

  public AppBitsUploadingStep createAppRoute(String spaceGuid, String subdomain)
      throws EnginePublicationException {

    LOGGER.info("Assigning route to app...");
    try {
      String domainGuid = getAvailableDomain();

      String appRoutesInfoJson = getAppRoutesInfo(subdomain, domainGuid);
      int routesNumber = JsonDataFetcher.getIntValue(appRoutesInfoJson, ROUTES_NUMBER_JSON_PATH);

      String routeGuid;
      if (routesNumber > 0) {
        LOGGER.info("Fetching routes for " + subdomain + " subdomain");
        routeGuid = JsonDataFetcher.getStringValue(appRoutesInfoJson, ROUTE_JSON_PATH);
      } else {
        LOGGER.info("No route exists. Creating new one.");
        routeGuid = createNewRoute(subdomain, domainGuid, spaceGuid);
      }
      LOGGER.info("Binding route " + routeGuid + " to app " + appGuid);
      bindRouteToApp(routeGuid, appGuid);
      return new AppBitsUploadingStep(cfApiUrl, cfRestTemplate, appGuid);
    } catch (IOException e) {
      throw new EnginePublicationException("Unable to create route for app " + subdomain, e);
    }

  }

  private String getAvailableDomain() throws JsonProcessingException, IOException {
    String cfDomainsUrl = cfApiUrl + SHARED_DOMAINS_ENDPOINT;
    ResponseEntity<String> response = cfRestTemplate.exchange(cfDomainsUrl, HttpMethod.GET,
        HttpCommunication.simpleJsonRequest(), String.class);
    String domainGuid = JsonDataFetcher.getStringValue(response.getBody(), DOMAIN_JSON_PATH);

    return domainGuid;
  }

  private String getAppRoutesInfo(String appName, String domainGuid) {
    String cfGetRoutesUrl = cfApiUrl + GET_ROUTES_ENDPOINT_TEMPLATE;

    ResponseEntity<String> response = cfRestTemplate.exchange(cfGetRoutesUrl, HttpMethod.GET,
        HttpCommunication.simpleJsonRequest(), String.class, appName, domainGuid);
    return response.getBody();
  }

  private String createNewRoute(String appName, String domainGuid, String spaceGuid)
      throws JsonProcessingException, IOException {

    String createRouteRequestBody = createRouteBody(appName, domainGuid, spaceGuid);

    String cfCreateRouteUrl = cfApiUrl + ROUTES_ENDPOINT;
    ResponseEntity<String> response = cfRestTemplate.exchange(cfCreateRouteUrl, HttpMethod.POST,
        HttpCommunication.postRequest(createRouteRequestBody), String.class);

    return JsonDataFetcher.getStringValue(response.getBody(), ROUTE_GUID_JSON_PATH);
  }

  private void bindRouteToApp(String routeGuid, String appGuid) {
    String cfBindRouteToAppUrl = cfApiUrl + BIND_ROUTE_TO_APP_ENDPOINT_TEMPLATE;
    cfRestTemplate.exchange(cfBindRouteToAppUrl, HttpMethod.PUT,
        HttpCommunication.simpleJsonRequest(), String.class, appGuid, routeGuid);
  }

  private String createRouteBody(String subdomain, String domainGuid, String spaceGuid) {
    ObjectMapper mapper = new ObjectMapper();

    ObjectNode requestBody = mapper.createObjectNode();
    requestBody.put("host", subdomain);
    requestBody.put("domain_guid", domainGuid);
    requestBody.put("space_guid", spaceGuid);

    return requestBody.toString();
  }
}
