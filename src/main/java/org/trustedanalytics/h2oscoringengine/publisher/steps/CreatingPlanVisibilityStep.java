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

import static org.trustedanalytics.h2oscoringengine.publisher.http.CloudFoundryEndpoints.GET_SERVICE_GUID_BY_NAME_ENDPOINT_TEMPLATE;
import static org.trustedanalytics.h2oscoringengine.publisher.http.CloudFoundryEndpoints.GET_SERVICE_PLANS_ENDPOINT_TEMPLATE;
import static org.trustedanalytics.h2oscoringengine.publisher.http.CloudFoundryEndpoints.SERVICE_PLAN_VISIBILITIES_ENDPOINT;
import static org.trustedanalytics.h2oscoringengine.publisher.http.CloudFoundryResponsesJsonPaths.FIRST_SERVICE_PLAN_GUID;
import static org.trustedanalytics.h2oscoringengine.publisher.http.CloudFoundryResponsesJsonPaths.SERVICE_GUID_JSON_PATH;

import java.io.IOException;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.trustedanalytics.h2oscoringengine.publisher.EnginePublicationException;
import org.trustedanalytics.h2oscoringengine.publisher.http.JsonHttpCommunication;
import org.trustedanalytics.h2oscoringengine.publisher.http.JsonDataFetcher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class CreatingPlanVisibilityStep {

  private final String cfApiUrl;
  private final RestTemplate cfRestTemplate;

  public CreatingPlanVisibilityStep(String cfApiUrl, RestTemplate cfRestTemplate) {
    this.cfApiUrl = cfApiUrl;
    this.cfRestTemplate = cfRestTemplate;
  }

  public void addServicePlanVisibility(String orgGuid, String serviceName)
      throws EnginePublicationException {
    try {
      String serviceGuid = getServiceGuidByName(serviceName);
      String planGuid = getServicePlanGuid(serviceGuid);

      String requestBody = prepareServiceVisibilityJsonRequest(planGuid, orgGuid);
      String cfPlanVisibilityUrl = cfApiUrl + SERVICE_PLAN_VISIBILITIES_ENDPOINT;

      cfRestTemplate.exchange(cfPlanVisibilityUrl, HttpMethod.POST,
          JsonHttpCommunication.postRequest(requestBody), String.class);
    } catch (IOException e) {
      throw new EnginePublicationException(
          "Unable to set service plan visibility for " + serviceName, e);
    }
  }

  private String getServiceGuidByName(String serviceName)
      throws JsonProcessingException, IOException {
    String cfServiceGuidUrl = cfApiUrl + GET_SERVICE_GUID_BY_NAME_ENDPOINT_TEMPLATE;
    ResponseEntity<String> response = cfRestTemplate.exchange(cfServiceGuidUrl, HttpMethod.GET,
        JsonHttpCommunication.simpleJsonRequest(), String.class, serviceName);

    return JsonDataFetcher.getStringValue(response.getBody(), SERVICE_GUID_JSON_PATH);

  }

  private String getServicePlanGuid(String serviceGuid)
      throws JsonProcessingException, IOException {
    String cfServicePlanUrl = cfApiUrl + GET_SERVICE_PLANS_ENDPOINT_TEMPLATE;
    ResponseEntity<String> response = cfRestTemplate.exchange(cfServicePlanUrl, HttpMethod.GET,
        JsonHttpCommunication.simpleJsonRequest(), String.class, serviceGuid);

    return JsonDataFetcher.getStringValue(response.getBody(), FIRST_SERVICE_PLAN_GUID);

  }

  private String prepareServiceVisibilityJsonRequest(String servicePlanGuid, String orgGuid) {

    ObjectMapper mapper = new ObjectMapper();

    ObjectNode json = mapper.createObjectNode();
    json.put("service_plan_guid", servicePlanGuid);
    json.put("organization_guid", orgGuid);

    return json.toString();
  }
}
