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

import static org.trustedanalytics.h2oscoringengine.publisher.http.CloudFoundryEndpoints.APP_BITS_ENDPOINT_TEMPLATE;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.trustedanalytics.h2oscoringengine.publisher.EnginePublicationException;
import org.trustedanalytics.h2oscoringengine.publisher.http.HttpCommunication;


public class AppBitsUploadingStep {

  private static final Logger LOGGER = LoggerFactory.getLogger(AppBitsUploadingStep.class);

  private final String cfApiUrl;
  private final RestTemplate cfRestTemplate;
  private final String appGuid;

  public AppBitsUploadingStep(String cfApiUrl, RestTemplate cfRestTemplate, String appGuid) {
    this.cfApiUrl = cfApiUrl;
    this.cfRestTemplate = cfRestTemplate;
    this.appGuid = appGuid;
  }


  public RegisteringInApplicationBrokerStep uploadBits(Path appBits)
      throws EnginePublicationException {
    prepareRestTemplateForMultipartRequest();
    LOGGER.info("Uploading bits for app " + appGuid + " from " + appBits);

    try {

      HttpEntity<MultiValueMap<String, Object>> request = prepareMutlipartRequest(appBits);

      String cfUploadAppUrl = cfApiUrl + APP_BITS_ENDPOINT_TEMPLATE;
      cfRestTemplate.exchange(cfUploadAppUrl, HttpMethod.PUT, request, String.class, appGuid);

      return new RegisteringInApplicationBrokerStep(appGuid, cfApiUrl, cfRestTemplate);
    } catch (IOException e) {
      throw new EnginePublicationException(
          "Unable to read application bits from " + appBits.toString(), e);
    }
  }

  private void prepareRestTemplateForMultipartRequest() {
    List<HttpMessageConverter<?>> converters =
        new ArrayList<>(Arrays.asList(new MappingJackson2HttpMessageConverter(),
            new ResourceHttpMessageConverter(), new FormHttpMessageConverter()));
    cfRestTemplate.getMessageConverters().addAll(converters);
  }

  /**
   * Prepares request to CF
   * <a href="https://apidocs.cloudfoundry.org/225/apps/uploads_the_bits_for_an_app.html">endpoint
   * </a>
   * 
   * @param dataPath
   * @return prepared request
   * @throws IOException
   */
  private HttpEntity<MultiValueMap<String, Object>> prepareMutlipartRequest(Path dataPath)
      throws IOException {
    HttpEntity<String> resourcesPart = prepareResourcesRequestPart();
    HttpEntity<ByteArrayResource> dataPart = prepareDataRequestPart(dataPath);

    MultiValueMap<String, Object> multiPartRequest = new LinkedMultiValueMap<>();
    multiPartRequest.add("resources", resourcesPart);
    multiPartRequest.add("application", dataPart);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);

    return new HttpEntity<>(multiPartRequest, headers);

  }

  /**
   * CloudFoundry API requires an array of files it already has in cache.
   * We're sending an empty array here. 
   * 
   * @return
   */
  private HttpEntity<String> prepareResourcesRequestPart() {
    String resourcesJson = "[]";
    return new HttpEntity<>(resourcesJson);
  }

  private HttpEntity<ByteArrayResource> prepareDataRequestPart(Path dataPath) throws IOException {

    ByteArrayResource data = prepareData(dataPath);
    HttpHeaders headers = HttpCommunication.zipHeaders();

    return new HttpEntity<>(data, headers);
  }

  private ByteArrayResource prepareData(Path dataPath) throws IOException {
    return new ByteArrayResource(Files.readAllBytes(dataPath)) {
      @Override
      public String getFilename() {
        return dataPath.getFileName().toString();
      }
    };
  }
}
