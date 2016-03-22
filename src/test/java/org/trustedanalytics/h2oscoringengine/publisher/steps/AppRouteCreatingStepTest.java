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

import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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

public class AppRouteCreatingStepTest {

  private final String testSpaceGuid = "aassa-adafd";
  private final String testSubdomain = "somesubdomain";
  private final String testCfApi = "skaflakjg";
  private final String testAppGuid = "adjsa-dgf-asg";
  private final String testRouteGuid = "wrrw-aswe";
  private final String testDomainGuid = "sasfa-hujhpiue";
  private final String testCfDomainsEndpoint =
      testCfApi + CloudFoundryEndpoints.SHARED_DOMAINS_ENDPOINT;
  private final String testCfGetRoutesEndpoint =
      testCfApi + CloudFoundryEndpoints.GET_ROUTES_ENDPOINT_TEMPLATE;
  private final String testCfCreateRouteEndpoint =
      testCfApi + CloudFoundryEndpoints.ROUTES_ENDPOINT;
  private final String testCfBindRouteEndpoint =
      testCfApi + CloudFoundryEndpoints.BIND_ROUTE_TO_APP_ENDPOINT_TEMPLATE;

  private RestTemplate restTemplateMock;
  private ResponseEntity<String> domainsResponseMock;
  private ResponseEntity<String> routesResponseMock;
  private ResponseEntity<String> routeCreatedResponseMock;
  private String validDomainsResponse =
      "{\"resources\":[{\"metadata\":{\"guid\":\"" + testDomainGuid + "\"}}]}";
  private String invalidDomainsResponse = "{{}";
  private String oneRouteResponse =
      "{\"total_results\":\"1\",\"resources\":[{\"metadata\":{\"guid\":\"" + testRouteGuid
          + "\"}}]}";
  private String noRouteResponse = "{\"total_results\":\"0\"}";
  private String createRouteRequestBody = "{\"host\":\"" + testSubdomain + "\",\"domain_guid\":\""
      + testDomainGuid + "\",\"space_guid\":\"" + testSpaceGuid + "\"}";
  private String routeCreatedResponse = "{\"metadata\":{\"guid\":\"" + testRouteGuid + "\"}}";

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Before
  public void setUp() {
    this.restTemplateMock = mock(RestTemplate.class);

    this.domainsResponseMock = mock(ResponseEntity.class);
    when(restTemplateMock.exchange(testCfDomainsEndpoint, HttpMethod.GET,
        HttpCommunication.simpleJsonRequest(), String.class)).thenReturn(domainsResponseMock);

    this.routesResponseMock = mock(ResponseEntity.class);
    when(restTemplateMock.exchange(testCfGetRoutesEndpoint, HttpMethod.GET,
        HttpCommunication.simpleJsonRequest(), String.class, testSubdomain, testDomainGuid))
            .thenReturn(routesResponseMock);

    this.routeCreatedResponseMock = mock(ResponseEntity.class);
    when(restTemplateMock.exchange(testCfCreateRouteEndpoint, HttpMethod.POST,
        HttpCommunication.postRequest(createRouteRequestBody), String.class))
            .thenReturn(routeCreatedResponseMock);
  }

  @Test
  public void createAppRoute_allCloudFoundryCallsOccured() throws Exception {
    // given
    AppRouteCreatingStep step = new AppRouteCreatingStep(restTemplateMock, testCfApi, testAppGuid);

    // when
    when(domainsResponseMock.getBody()).thenReturn(validDomainsResponse);
    when(routesResponseMock.getBody()).thenReturn(oneRouteResponse);

    step.createAppRoute(testSpaceGuid, testSubdomain);

    // then
    verify(restTemplateMock).exchange(eq(testCfDomainsEndpoint), same(HttpMethod.GET),
        eq(HttpCommunication.simpleJsonRequest()), same(String.class));
    verify(restTemplateMock).exchange(eq(testCfGetRoutesEndpoint), same(HttpMethod.GET),
        eq(HttpCommunication.simpleJsonRequest()), same(String.class), eq(testSubdomain),
        eq(testDomainGuid));
    verify(restTemplateMock).exchange(eq(testCfBindRouteEndpoint), same(HttpMethod.PUT),
        eq(HttpCommunication.simpleJsonRequest()), same(String.class), eq(testAppGuid),
        eq(testRouteGuid));


  }

  @Test
  public void createAppRoute_oneRouteFound_dontCreateAnotherRoute() throws Exception {
    // given
    AppRouteCreatingStep step = new AppRouteCreatingStep(restTemplateMock, testCfApi, testAppGuid);

    // when
    when(domainsResponseMock.getBody()).thenReturn(validDomainsResponse);
    when(routesResponseMock.getBody()).thenReturn(oneRouteResponse);

    step.createAppRoute(testSpaceGuid, testSubdomain);

    // then
    verify(restTemplateMock, never()).exchange(eq(testCfCreateRouteEndpoint), same(HttpMethod.POST),
        eq(HttpCommunication.postRequest(createRouteRequestBody)), same(String.class));
  }

  @Test
  public void createAppRoute_noRouteFound_createNewRouteCfCallOccured() throws Exception {
    // given
    AppRouteCreatingStep step = new AppRouteCreatingStep(restTemplateMock, testCfApi, testAppGuid);

    // when
    when(domainsResponseMock.getBody()).thenReturn(validDomainsResponse);
    when(routesResponseMock.getBody()).thenReturn(noRouteResponse);
    when(routeCreatedResponseMock.getBody()).thenReturn(routeCreatedResponse);

    step.createAppRoute(testSpaceGuid, testSubdomain);

    // then
    verify(restTemplateMock).exchange(eq(testCfCreateRouteEndpoint), same(HttpMethod.POST),
        eq(HttpCommunication.postRequest(createRouteRequestBody)), same(String.class));
  }

  @Test
  public void createAppRoute_invalidCfResponse_exceptionThrown() throws EnginePublicationException {
    // given
    AppRouteCreatingStep step = new AppRouteCreatingStep(restTemplateMock, testCfApi, testAppGuid);

    // when
    when(domainsResponseMock.getBody()).thenReturn(invalidDomainsResponse);

    // then
    thrown.expect(EnginePublicationException.class);
    step.createAppRoute(testSpaceGuid, testSubdomain);
  }
}
