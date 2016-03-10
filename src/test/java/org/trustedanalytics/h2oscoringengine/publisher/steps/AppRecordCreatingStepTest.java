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

public class AppRecordCreatingStepTest {

  private final String testCfApi = "skaflakjg";
  private final String cfAppsEndpoint = testCfApi + CloudFoundryEndpoints.APPS_ENDPOINT;
  private final String testAppGuid = "adjsa-dgf-asg";
  private final String spaceGuid = "asfajkhghga-dgag";
  private final String appName = "sagsdg";
  private final String expectedRequestBody =
      "{\"name\":\"" + appName + "\",\"space_guid\":\"" + spaceGuid + "\"}";

  private RestTemplate restTemplateMock;
  private ResponseEntity<String> responseMock;
  private String validResponseMock =
      "{\"metadata\":{\"guid\":\"" + testAppGuid + "\",\"something\":\"something\"}}";
  private String invalidResponseMock = "{{}";


  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Before
  public void setUp() {
    this.restTemplateMock = mock(RestTemplate.class);
    this.responseMock = mock(ResponseEntity.class);
    when(restTemplateMock.exchange(eq(cfAppsEndpoint), same(HttpMethod.POST), any(),
        same(String.class))).thenReturn(responseMock);
  }

  @Test
  public void createAppRecord_callToCfApiOccured() throws Exception {
    // given
    AppRecordCreatingStep step = new AppRecordCreatingStep(testCfApi, restTemplateMock);

    // when
    when(responseMock.getBody()).thenReturn(validResponseMock);
    step.createAppRecord(spaceGuid, appName);

    // then
    verify(restTemplateMock).exchange(eq(cfAppsEndpoint), same(HttpMethod.POST),
        eq(HttpCommunication.postRequest(expectedRequestBody)), eq(String.class));
  }

  @Test
  public void createAppRecord_invalidResponse_exceptionThrown() throws EnginePublicationException {
    // given
    AppRecordCreatingStep step = new AppRecordCreatingStep(testCfApi, restTemplateMock);

    // when
    when(responseMock.getBody()).thenReturn(invalidResponseMock);

    // then
    thrown.expect(EnginePublicationException.class);
    step.createAppRecord(spaceGuid, appName);

  }
}
