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

import static org.trustedanalytics.h2oscoringengine.publisher.http.CloudFoundryEndpoints.APP_BROKER_CATALOG_ENDPOINT;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;
import org.trustedanalytics.h2oscoringengine.publisher.http.BasicAuthServerCredentials;
import org.trustedanalytics.h2oscoringengine.publisher.http.JsonHttpCommunication;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class RegisteringInApplicationBrokerStep {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(RegisteringInApplicationBrokerStep.class);

  private final String appGuid;
  private final String cfApiUrl;
  private final RestTemplate cfRestTemplate;

  public RegisteringInApplicationBrokerStep(String appGuid, String cfApiUrl,
      RestTemplate cfRestTemplate) {
    this.appGuid = appGuid;
    this.cfApiUrl = cfApiUrl;
    this.cfRestTemplate = cfRestTemplate;
  }

  public CreatingPlanVisibilityStep register(BasicAuthServerCredentials appBrokerCredentials,
      String serviceName, String serviceDescription) {

    LOGGER.info("Registering service " + serviceName + " in application-broker");

    String requestBody = prepareAppBrokerJsonRequest(serviceName, serviceDescription);
    HttpHeaders headers =
        JsonHttpCommunication.basicAuthJsonHeaders(appBrokerCredentials.getBasicAuthToken());
    HttpEntity<String> request = new HttpEntity<String>(requestBody, headers);

    String appBrokerEndpoint = appBrokerCredentials.getHost() + APP_BROKER_CATALOG_ENDPOINT;
    new RestTemplate().exchange(appBrokerEndpoint, HttpMethod.POST, request, String.class);

    return new CreatingPlanVisibilityStep(cfApiUrl, cfRestTemplate);

  }

  private String prepareAppBrokerJsonRequest(String serviceName, String serviceDescription) {
    String planId = UUID.randomUUID().toString();
    String serviceId = UUID.randomUUID().toString();

    ObjectMapper mapper = new ObjectMapper();

    ArrayNode plansArray = mapper.createArrayNode();
    ObjectNode planNode = mapper.createObjectNode();
    planNode.put("id", planId);
    plansArray.add(planNode);

    ObjectNode guidNode = mapper.createObjectNode();
    guidNode.put("guid", appGuid);

    ObjectNode metadataNode = mapper.createObjectNode();
    metadataNode.set("metadata", guidNode);

    ObjectNode json = mapper.createObjectNode();
    json.set("app", metadataNode);
    json.put("id", serviceId);
    json.set("plans", plansArray);
    json.put("description", serviceDescription);
    json.put("name", serviceName);

    return json.toString();
  }
}
