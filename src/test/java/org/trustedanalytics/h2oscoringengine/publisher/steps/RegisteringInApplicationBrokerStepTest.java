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

import static org.mockito.Mockito.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.trustedanalytics.h2oscoringengine.publisher.http.CloudFoundryEndpoints.APP_BROKER_CATALOG_ENDPOINT;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;
import org.trustedanalytics.h2oscoringengine.publisher.http.BasicAuthServerCredentials;
import org.trustedanalytics.h2oscoringengine.publisher.http.HttpCommunication;
import org.trustedanalytics.h2oscoringengine.publisher.http.JsonDataFetcher;

public class RegisteringInApplicationBrokerStepTest {

  private final String testCfApi = "skaflakjg";
  private final String testAppGuid = "adjsa-dgf-asg";
  private final String testServiceName = "sakj";
  private final String testServiceDescription = "Asklj aslkjgka akdjgakgj adkjgj";
  private BasicAuthServerCredentials testCredentials =
      new BasicAuthServerCredentials("somehost", "username", "password");
  private String appBrokerEndpoint = testCredentials.getHost() + APP_BROKER_CATALOG_ENDPOINT;
  
  private RestTemplate cfRestTemplateMock = mock(RestTemplate.class);
  private RestTemplate basicRestTemplateMock = mock(RestTemplate.class);
  
  private HttpHeaders expectedHeaders =
      HttpCommunication.basicAuthJsonHeaders(testCredentials.getBasicAuthToken());

  @Test
  public void register_callToAppBrokerOccured() throws Exception {
    // given
    RegisteringInApplicationBrokerStep step = new RegisteringInApplicationBrokerStep(testAppGuid,
        testCfApi, cfRestTemplateMock, basicRestTemplateMock);

    // when
    step.register(testCredentials, testServiceName, testServiceDescription);
    ArgumentCaptor<HttpEntity> requestCaptor = ArgumentCaptor.forClass(HttpEntity.class);

    // then
    verify(basicRestTemplateMock).exchange(eq(appBrokerEndpoint), same(HttpMethod.POST),
        requestCaptor.capture(), same(String.class));
    HttpEntity<String> request = requestCaptor.getValue();
    assertThat(request.getHeaders(), equalTo(expectedHeaders));
    String actualRequestBody = request.getBody();
    assertThat(JsonDataFetcher.getStringValue(actualRequestBody, "/app/metadata/guid"),
        equalTo(testAppGuid));
    assertThat(JsonDataFetcher.getStringValue(actualRequestBody, "/description"),
        equalTo(testServiceDescription));
    assertThat(JsonDataFetcher.getStringValue(actualRequestBody, "/name"),
        equalTo(testServiceName));

  }
}
