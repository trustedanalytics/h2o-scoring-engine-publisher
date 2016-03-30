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
package org.trustedanalytics.h2oscoringengine.publisher;

import org.springframework.web.client.RestTemplate;
import org.trustedanalytics.h2oscoringengine.publisher.http.BasicAuthServerCredentials;

public class AppBrokerConnectionData {

  private final RestTemplate appBrokerRestTemplate;
  private final BasicAuthServerCredentials appBrokerCredentials;

  public AppBrokerConnectionData(RestTemplate appBrokerRestTemplate, BasicAuthServerCredentials appBrokerCredentials) {
    this.appBrokerRestTemplate = appBrokerRestTemplate;
    this.appBrokerCredentials = appBrokerCredentials;
  }

  public BasicAuthServerCredentials getAppBrokerCredentials() {
    return appBrokerCredentials;
  }

  public RestTemplate getAppBrokerRestTemplate() {
    return appBrokerRestTemplate;
  }
}
