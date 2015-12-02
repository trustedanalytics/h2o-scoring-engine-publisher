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

import static org.trustedanalytics.h2oscoringengine.publisher.http.CloudFoundryEndpoints.APP_IN_SPACE_ENDPOINT_TEMPLATE;
import static org.trustedanalytics.h2oscoringengine.publisher.http.CloudFoundryResponsesJsonPaths.APPS_NUMBER_JSON_PATH;
import static org.trustedanalytics.h2oscoringengine.publisher.http.JsonHttpCommunication.createSimpleJsonRequest;
import static org.trustedanalytics.h2oscoringengine.publisher.http.JsonHttpCommunication.getIntValueFromJson;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.trustedanalytics.h2oscoringengine.publisher.EnginePublicationException;

public class CheckingIfAppExistsStep {

  private static final Logger LOGGER = LoggerFactory.getLogger(CheckingIfAppExistsStep.class);

  private final RestTemplate cfRestTemplate;
  private final String cfAppInSpaceUrl;

  public CheckingIfAppExistsStep(String cfApiUrl, RestTemplate cfRestTemplate) {
    this.cfRestTemplate = cfRestTemplate;
    this.cfAppInSpaceUrl = cfApiUrl + APP_IN_SPACE_ENDPOINT_TEMPLATE;
  }


  public boolean check(String appName, String spaceGuid) throws EnginePublicationException {

    ResponseEntity<String> response = cfRestTemplate.exchange(cfAppInSpaceUrl, HttpMethod.GET,
        createSimpleJsonRequest(), String.class, spaceGuid, appName);

    Integer appsNumber;
    try {
      appsNumber = getIntValueFromJson(response.getBody(), APPS_NUMBER_JSON_PATH);
      LOGGER.debug("Number of found apps: " + appsNumber);
      return appsNumber != 0;
    } catch (IOException e) {
      throw new EnginePublicationException("Unable to check if CloudFoundry app already exists: ",
          e);
    }
  }
}
