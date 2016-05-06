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

import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.trustedanalytics.h2oscoringengine.publisher.http.CloudFoundryEndpoints.APPS_ENDPOINT;
import static org.trustedanalytics.h2oscoringengine.publisher.http.CloudFoundryEndpoints.APP_BITS_ENDPOINT_TEMPLATE;
import static org.trustedanalytics.h2oscoringengine.publisher.http.CloudFoundryEndpoints.APP_BROKER_CATALOG_ENDPOINT;
import static org.trustedanalytics.h2oscoringengine.publisher.http.CloudFoundryEndpoints.APP_IN_SPACE_ENDPOINT_TEMPLATE;
import static org.trustedanalytics.h2oscoringengine.publisher.http.CloudFoundryEndpoints.BIND_ROUTE_TO_APP_ENDPOINT_TEMPLATE;
import static org.trustedanalytics.h2oscoringengine.publisher.http.CloudFoundryEndpoints.GET_ROUTES_ENDPOINT_TEMPLATE;
import static org.trustedanalytics.h2oscoringengine.publisher.http.CloudFoundryEndpoints.GET_SERVICE_GUID_BY_NAME_ENDPOINT_TEMPLATE;
import static org.trustedanalytics.h2oscoringengine.publisher.http.CloudFoundryEndpoints.GET_SERVICE_PLANS_ENDPOINT_TEMPLATE;
import static org.trustedanalytics.h2oscoringengine.publisher.http.CloudFoundryEndpoints.SERVICE_PLAN_VISIBILITIES_ENDPOINT;
import static org.trustedanalytics.h2oscoringengine.publisher.http.CloudFoundryEndpoints.SHARED_DOMAINS_ENDPOINT;

import java.io.IOException;
import java.nio.file.Files;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import org.trustedanalytics.h2oscoringengine.publisher.http.BasicAuthServerCredentials;
import org.trustedanalytics.h2oscoringengine.publisher.restapi.PublishRequest;
import org.trustedanalytics.h2oscoringengine.publisher.steps.H2oResourcesDownloadingStep;


public class PublisherIntegrationTest {

  private MockRestServiceServer cfServerMock;
  private RestTemplate cfRestTemplate = new RestTemplate();
  private String testCfApi = "cloudfoundry-jkjkafsjka";

  private MockRestServiceServer h2oServerMock;
  private RestTemplate h2oRestTemplate = new RestTemplate();
  private final String testH2oServerUrl = "h2o-server-lkkajjdk";
  private final String testH2oUser = "akljfashf";
  private final String testH2oPassword = "askjfsl";

  private MockRestServiceServer appBrokerMock;
  private RestTemplate appBrokerRestTemplate = new RestTemplate();
  private final String testAppBrokerHost = "app-broker-kkjljklkjl";
  private final String testAppBrokerUsername = "akjf";
  private final String testAppBrokerPassword = "oiup";

  private String engineBaseResourcePath = "/runtime/h2o-scoring-engine-base-0.5.0.jar";

  private PublishRequest testPublishRequest = new PublishRequest();
  private BasicAuthServerCredentials testH2oCredentials = new BasicAuthServerCredentials(testH2oServerUrl, testH2oUser, testH2oPassword);
  private final String testModelName = "some_model";
  private final String testOrgGuid = "kasjf-azffd-adafd";
  private final String testTechnicalSpaceGuid = "oiouio-oiiooip";
  private final String testAppGuid = "kllklk-jkjkjkl";
  private final String testDomainGuid = "pwiiweu-oiuioio";
  private final String testRouteGuid = "isaef-iopas";
  private final String testServiceGuid = "opiiop-oiopiop";
  private final String testPlanGuid = "jksjfk-jkjkljkl";

  // expected requests
  private final String appExistsRequest =
      APP_IN_SPACE_ENDPOINT_TEMPLATE.replaceAll("\\{spaceGuid\\}", testTechnicalSpaceGuid)
          .replaceAll("\\{appName\\}", testModelName);
  private final String getModelRequest =
      H2oResourcesDownloadingStep.H2O_SERVER_MODEL_PATH_PREFIX + testModelName;
  private final String getLibRequest = H2oResourcesDownloadingStep.H2O_SERVER_LIB_PATH;
  private final String getRouteRequest = GET_ROUTES_ENDPOINT_TEMPLATE
      .replaceAll("\\{name\\}", testModelName).replaceAll("\\{guid\\}", testDomainGuid);
  private final String bindRouteEndpoint = BIND_ROUTE_TO_APP_ENDPOINT_TEMPLATE
      .replaceAll("\\{appGuid\\}", testAppGuid).replaceAll("\\{routeGuid\\}", testRouteGuid);
  private final String uploadAppBitsEndpoint =
      APP_BITS_ENDPOINT_TEMPLATE.replaceAll("\\{appGuid\\}", testAppGuid);
  private final String getServiceGuidByNameEndpoint =
      GET_SERVICE_GUID_BY_NAME_ENDPOINT_TEMPLATE.replaceAll("\\{serviceName\\}", testModelName);
  private final String servicePlanEndpoint =
      GET_SERVICE_PLANS_ENDPOINT_TEMPLATE.replaceAll("\\{serviceGuid\\}", testServiceGuid);
  private final String servicePlanVisibilityEndpoint = SERVICE_PLAN_VISIBILITIES_ENDPOINT;

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Before
  public void setUp() {
    cfServerMock = MockRestServiceServer.createServer(cfRestTemplate);
    h2oServerMock = MockRestServiceServer.createServer(h2oRestTemplate);
    appBrokerMock = MockRestServiceServer.createServer(appBrokerRestTemplate);

    testPublishRequest.setH2oCredentials(testH2oCredentials);
    testPublishRequest.setModelName(testModelName);
    testPublishRequest.setOrgGuid(testOrgGuid);
  }

  @Test
  public void publish_cfAppExists_exceptionThrown() throws Exception {
    // given
    Publisher publisher = new Publisher(
        new CfConnectionData(cfRestTemplate, testCfApi, testTechnicalSpaceGuid), h2oRestTemplate,
        new AppBrokerConnectionData(appBrokerRestTemplate, new BasicAuthServerCredentials(
            testAppBrokerHost, testAppBrokerUsername, testAppBrokerPassword)),
        engineBaseResourcePath);
    cfServerMock.expect(requestTo(testCfApi + appExistsRequest)).andExpect(method(HttpMethod.GET))
        .andRespond(withSuccess("{\"total_results\": 1}", MediaType.APPLICATION_JSON));

    // then
    thrown.expect(EnginePublicationException.class);

    // when
    publisher.publish(testPublishRequest);
  }

  @Test
  public void publish_allRequestsOccured() throws Exception {
    // given
    Publisher publisher = new Publisher(
        new CfConnectionData(cfRestTemplate, testCfApi, testTechnicalSpaceGuid), h2oRestTemplate,
        new AppBrokerConnectionData(appBrokerRestTemplate, new BasicAuthServerCredentials(
            testAppBrokerHost, testAppBrokerUsername, testAppBrokerPassword)),
        engineBaseResourcePath);
    setCfApiExpectedCalls();
    setH2oServerExpectedCalls();
    setAppBrokerExpectedCalls();

    // when
    publisher.publish(testPublishRequest);

    // then
    cfServerMock.verify();
    h2oServerMock.verify();
    appBrokerMock.verify();
  }

  @Test
  public void getScoringEngineJar_h2oRequestsOccured() throws Exception {
    // given
    Publisher publisher = new Publisher(
        new CfConnectionData(cfRestTemplate, testCfApi, testTechnicalSpaceGuid), h2oRestTemplate,
        new AppBrokerConnectionData(appBrokerRestTemplate, new BasicAuthServerCredentials(
            testAppBrokerHost, testAppBrokerUsername, testAppBrokerPassword)),
        engineBaseResourcePath);
    setH2oServerExpectedCalls();

    // when
    publisher.getScoringEngineJar(testH2oCredentials, testModelName);
    
    //then
    h2oServerMock.verify();
  }

  private void setCfApiExpectedCalls() {
    // checking if app exists
    cfServerMock.expect(requestTo(testCfApi + appExistsRequest)).andExpect(method(HttpMethod.GET))
        .andRespond(withSuccess("{\"total_results\": 0}", MediaType.APPLICATION_JSON));
    // creating app record
    cfServerMock.expect(requestTo(testCfApi + APPS_ENDPOINT))
        .andRespond(withSuccess(
            "{\"metadata\":{\"guid\":\"" + testAppGuid + "\",\"something\":\"something\"}}",
            MediaType.APPLICATION_JSON));
    // getting available domains
    cfServerMock.expect(requestTo(testCfApi + SHARED_DOMAINS_ENDPOINT)).andRespond(
        withSuccess("{\"resources\":[{\"metadata\":{\"guid\":\"" + testDomainGuid + "\"}}]}",
            MediaType.APPLICATION_JSON));
    // getting route for app
    cfServerMock.expect(requestTo(testCfApi + getRouteRequest))
        .andRespond(withSuccess("{\"total_results\":\"1\",\"resources\":[{\"metadata\":{\"guid\":\""
            + testRouteGuid + "\"}}]}", MediaType.APPLICATION_JSON));
    // binding route to app
    cfServerMock.expect(requestTo(testCfApi + bindRouteEndpoint)).andRespond(withSuccess(
        "{\"metadata\":{\"guid\":\"" + testRouteGuid + "\"}}", MediaType.APPLICATION_JSON));
    // uploading app bits
    cfServerMock.expect(requestTo(testCfApi + uploadAppBitsEndpoint))
        .andExpect(method(HttpMethod.PUT)).andRespond(withSuccess());
    // getting service guid
    cfServerMock.expect(requestTo(testCfApi + getServiceGuidByNameEndpoint)).andRespond(
        withSuccess("{\"resources\":[{\"metadata\":{\"guid\":\"" + testServiceGuid + "\"}}]}",
            MediaType.APPLICATION_JSON));
    // getting service plan guid
    cfServerMock.expect(requestTo(testCfApi + servicePlanEndpoint)).andRespond(
        withSuccess("{\"resources\":[{\"metadata\":{\"guid\":\"" + testPlanGuid + "\"}}]}",
            MediaType.APPLICATION_JSON));
    // setting service plan visibility
    cfServerMock.expect(requestTo(testCfApi + servicePlanVisibilityEndpoint))
        .andRespond(withSuccess());

  }

  private void setH2oServerExpectedCalls() throws IOException {
    h2oServerMock.expect(requestTo(testH2oServerUrl + getModelRequest))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withSuccess(prepareModelJavaFile(), MediaType.APPLICATION_OCTET_STREAM));
    h2oServerMock.expect(requestTo(testH2oServerUrl + getLibRequest))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withSuccess(prepareGenModelLib(), MediaType.APPLICATION_OCTET_STREAM));
  }

  private void setAppBrokerExpectedCalls() {
    appBrokerMock.expect(requestTo(testAppBrokerHost + APP_BROKER_CATALOG_ENDPOINT))
        .andRespond(withSuccess());
  }

  private byte[] prepareModelJavaFile() throws IOException {
    TestCompilationResourcesBuilder compilationResourcesBuilder =
        new TestCompilationResourcesBuilder();
    return Files.readAllBytes(compilationResourcesBuilder.prepareModelJavaFile(testModelName));
  }

  private byte[] prepareGenModelLib() throws IOException {
    TestCompilationResourcesBuilder compilationResourcesBuilder =
        new TestCompilationResourcesBuilder();
    return Files.readAllBytes(compilationResourcesBuilder.prepareLibraryFile());
  }
}
