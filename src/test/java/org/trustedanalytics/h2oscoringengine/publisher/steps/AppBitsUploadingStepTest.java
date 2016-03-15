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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.trustedanalytics.h2oscoringengine.publisher.EnginePublicationException;
import org.trustedanalytics.h2oscoringengine.publisher.http.CloudFoundryEndpoints;
import org.trustedanalytics.h2oscoringengine.publisher.http.HttpCommunication;

public class AppBitsUploadingStepTest {

  private Path testAppBitsPath;
  private final String testCfApi = "skaflakjg";
  private final String cfUploadEndpoint = testCfApi + CloudFoundryEndpoints.APP_BITS_ENDPOINT_TEMPLATE;
  private final String testAppGuid = "adjsa-dgf-asg";
  private final RestTemplate restTemplateMock = mock(RestTemplate.class);

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Before
  public void setUp() throws IOException {
    this.testAppBitsPath = testFile();
  }

  @Test
  public void uploadBits_performsProperCloudFoundryRestCall() throws Exception {
    // given
    AppBitsUploadingStep step = new AppBitsUploadingStep(testCfApi, restTemplateMock, testAppGuid);

    // when
    step.uploadBits(testAppBitsPath);

    // then
    HttpEntity<MultiValueMap<String, Object>> testRequest = createTestAppBitsRequest();

    verify(restTemplateMock).exchange(eq(cfUploadEndpoint), same(HttpMethod.PUT), eq(testRequest),
        same(String.class), same(testAppGuid));
  }

  @Test
  public void uploadBits_unableToReadFile_exceptionThrown() throws EnginePublicationException {
    // given
    AppBitsUploadingStep step = new AppBitsUploadingStep(testCfApi, restTemplateMock, testAppGuid);

    // when
    Path appBits = nonExistentTestFile();

    // then
    thrown.expect(EnginePublicationException.class);
    step.uploadBits(appBits);
  }

  private HttpEntity<MultiValueMap<String, Object>> createTestAppBitsRequest() throws IOException {
    HttpEntity<String> resourcesPart = new HttpEntity<String>("[]");
    HttpEntity<ByteArrayResource> dataPart = new HttpEntity<>(
        new ByteArrayResource(Files.readAllBytes(testAppBitsPath)), HttpCommunication.zipHeaders());

    MultiValueMap<String, Object> multiPartRequest = new LinkedMultiValueMap<>();
    multiPartRequest.add("resources", resourcesPart);
    multiPartRequest.add("application", dataPart);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);

    return new HttpEntity<>(multiPartRequest, headers);
  }

  private Path testFile() throws IOException {
    return Files.createTempFile("test", "");
  }

  private Path nonExistentTestFile() {
    return Paths.get("safggrdga");
  }


}
