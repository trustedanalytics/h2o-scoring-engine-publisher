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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.trustedanalytics.h2oscoringengine.publisher.EnginePublicationException;
import org.trustedanalytics.h2oscoringengine.publisher.http.CloudFoundryEndpoints;
import org.trustedanalytics.h2oscoringengine.publisher.http.HttpCommunication;

public class CheckingIfAppExistsStepTest {

  private final String testCfApi = "ajksfpiuj";
  private final String testCfAppInSpaceEndpoint =
      testCfApi + CloudFoundryEndpoints.APP_IN_SPACE_ENDPOINT_TEMPLATE;
  private final String testAppName = "oiupaueh";
  private final String testSpaceGuid = "sjakdhljk";

  private RestTemplate restTemplateMock;
  private ResponseEntity<String> appsNumberResponseMock;
  private final String appExistsResponse = "{\"total_results\": 1}";
  private final String appNotExistsResponse = "{\"total_results\": 0}";
  private final String invalidAppsNumberResponse = "{{}";

  @Rule
  public final ExpectedException thrown = ExpectedException.none();
  
  @Before
  public void setUp() {
    this.restTemplateMock = mock(RestTemplate.class);
    this.appsNumberResponseMock = mock(ResponseEntity.class);
    when(restTemplateMock.exchange(testCfAppInSpaceEndpoint, HttpMethod.GET,
        HttpCommunication.simpleJsonRequest(), String.class, testSpaceGuid, testAppName))
            .thenReturn(appsNumberResponseMock);
  }

  @Test
  public void check_appsFound_returnsTrue() throws Exception {
    // given
    CheckingIfAppExistsStep step = new CheckingIfAppExistsStep(testCfApi, restTemplateMock);

    // when
    when(appsNumberResponseMock.getBody()).thenReturn(appExistsResponse);
    boolean result = step.check(testAppName, testSpaceGuid);

    // then
    assertThat(result, is(true));
  }
  
  @Test
  public void check_noAppsFound_returnsFalse() throws Exception {
    // given
    CheckingIfAppExistsStep step = new CheckingIfAppExistsStep(testCfApi, restTemplateMock);

    // when
    when(appsNumberResponseMock.getBody()).thenReturn(appNotExistsResponse);
    boolean result = step.check(testAppName, testSpaceGuid);

    // then
    assertThat(result, is(false));
  }
  
  @Test
  public void check_cannotGetValidAnswerFromCf_exceptionThrown() throws Exception {
    // given
    CheckingIfAppExistsStep step = new CheckingIfAppExistsStep(testCfApi, restTemplateMock);

    // when
    when(appsNumberResponseMock.getBody()).thenReturn(invalidAppsNumberResponse);

    // then
    thrown.expect(EnginePublicationException.class);
    step.check(testAppName, testSpaceGuid);
  }


}
