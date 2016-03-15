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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.trustedanalytics.h2oscoringengine.publisher.http.CloudFoundryEndpoints.GET_SERVICE_GUID_BY_NAME_ENDPOINT_TEMPLATE;
import static org.trustedanalytics.h2oscoringengine.publisher.http.CloudFoundryEndpoints.GET_SERVICE_PLANS_ENDPOINT_TEMPLATE;
import static org.trustedanalytics.h2oscoringengine.publisher.http.CloudFoundryEndpoints.SERVICE_PLAN_VISIBILITIES_ENDPOINT;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.trustedanalytics.h2oscoringengine.publisher.EnginePublicationException;
import org.trustedanalytics.h2oscoringengine.publisher.http.HttpCommunication;

public class CreatingPlanVisibilityStepTest {

  private final String testCfApi = "ajksfpiuj";
  private final String testOrgGuid = "akj-haeiu-zjkhdfg";
  private final String testServiceName = "ajfhjskhfk";
  private final String testServiceGuid = "sas-dfdfg-gfsfgd";
  private final String testPlanGuid = "asdfkj-adg-asjkh";
  private final String testCfServiceGuidEndpoint =
      testCfApi + GET_SERVICE_GUID_BY_NAME_ENDPOINT_TEMPLATE;
  private final String testCfServicePlanEndpoint = testCfApi + GET_SERVICE_PLANS_ENDPOINT_TEMPLATE;
  private final String testCfPlanVisibilityEndpoint =
      testCfApi + SERVICE_PLAN_VISIBILITIES_ENDPOINT;
  private final String setVisibilityRequestBody = "{\"service_plan_guid\":\"" + testPlanGuid
      + "\",\"organization_guid\":\"" + testOrgGuid + "\"}";

  private RestTemplate restTemplateMock;
  private ResponseEntity<String> serviceGuidResponseMock;
  private ResponseEntity<String> servicePlanResponseMock;
  private final String serviceGuidResponse =
      "{\"resources\":[{\"metadata\":{\"guid\":\"" + testServiceGuid + "\"}}]}";
  private final String planGuidResponse =
      "{\"resources\":[{\"metadata\":{\"guid\":\"" + testPlanGuid + "\"}}]}";
  private final String invalidPlanGuidResponse= "{{}";

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Before
  public void setUp() {
    this.restTemplateMock = mock(RestTemplate.class);

    this.serviceGuidResponseMock = mock(ResponseEntity.class);
    when(restTemplateMock.exchange(eq(testCfServiceGuidEndpoint), same(HttpMethod.GET),
        eq(HttpCommunication.simpleJsonRequest()), same(String.class), eq(testServiceName)))
            .thenReturn(serviceGuidResponseMock);

    this.servicePlanResponseMock = mock(ResponseEntity.class);
    when(restTemplateMock.exchange(eq(testCfServicePlanEndpoint), same(HttpMethod.GET),
        eq(HttpCommunication.simpleJsonRequest()), same(String.class), eq(testServiceGuid)))
            .thenReturn(servicePlanResponseMock);
  }

  @Test
  public void addServicePlanVisibility_allCloduFoundryCallsOccured() throws Exception {
    // given
    CreatingPlanVisibilityStep step = new CreatingPlanVisibilityStep(testCfApi, restTemplateMock);

    // when
    when(serviceGuidResponseMock.getBody()).thenReturn(serviceGuidResponse);
    when(servicePlanResponseMock.getBody()).thenReturn(planGuidResponse);
    step.addServicePlanVisibility(testOrgGuid, testServiceName);

    // then
    verify(restTemplateMock).exchange(testCfServiceGuidEndpoint, HttpMethod.GET,
        HttpCommunication.simpleJsonRequest(), String.class, testServiceName);
    verify(restTemplateMock).exchange(testCfServicePlanEndpoint, HttpMethod.GET,
        HttpCommunication.simpleJsonRequest(), String.class, testServiceGuid);
    verify(restTemplateMock).exchange(testCfPlanVisibilityEndpoint, HttpMethod.POST,
        HttpCommunication.postRequest(setVisibilityRequestBody), String.class);

  }

  @Test
  public void addServicePlanVisibility_invalidCloudFoundryResponse_exceptionThrown()
      throws Exception {
    // given
    CreatingPlanVisibilityStep step = new CreatingPlanVisibilityStep(testCfApi, restTemplateMock);

    // when
    when(serviceGuidResponseMock.getBody()).thenReturn(serviceGuidResponse);
    when(servicePlanResponseMock.getBody()).thenReturn(invalidPlanGuidResponse);

    // then
    thrown.expect(EnginePublicationException.class);
    step.addServicePlanVisibility(testOrgGuid, testServiceName);
  }

}
