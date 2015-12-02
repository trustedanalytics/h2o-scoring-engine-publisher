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
package org.trustedanalytics.h2oscoringengine.publisher.http;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.trustedanalytics.h2oscoringengine.publisher.EngineBuildingException;

/**
 * Downloads files from BasicAuth secured REST server.
 */
public class FilesDownloader {

  private static final Logger LOGGER = LoggerFactory.getLogger(FilesDownloader.class);

  private String basicAuthToken;
  private String serverUrl;

  public FilesDownloader(BasicAuthServerCredentials serverCredentials) {
    this.serverUrl = serverCredentials.getHost();
    this.basicAuthToken = serverCredentials.getBasicAuthToken();
  }

  public Path download(String resourcePath, Path destinationFilePath)
      throws EngineBuildingException {

    RestTemplate restTemplate = new RestTemplate();
    restTemplate.getMessageConverters().add(new ByteArrayHttpMessageConverter());

    String resourceUrl = serverUrl + resourcePath;

    LOGGER.info("Downloading " + resourceUrl);
    
    try {
      ResponseEntity<byte[]> response = restTemplate.exchange(resourceUrl, HttpMethod.GET,
          prepareBasicAuthRequest(), byte[].class);
      return writeBytes(destinationFilePath, response.getBody());
      
    } catch (HttpClientErrorException e) {
      String errorMessage = prepareErrorMessage(e.getStatusCode(), resourceUrl);
      LOGGER.error(errorMessage);
      throw new EngineBuildingException(errorMessage);
    }

  }

  private HttpEntity<String> prepareBasicAuthRequest() {
    HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Basic " + basicAuthToken);

    return new HttpEntity<String>(headers);
  }

  private Path writeBytes(Path destinationFilePath, byte[] bytes) throws EngineBuildingException {
    try {
      return Files.write(destinationFilePath, bytes);
    } catch (IOException e) {
      throw new EngineBuildingException("Unable to write files " + e);
    }
  }

  private String prepareErrorMessage(HttpStatus httpStatus, String resourceUrl) {
    String errorMessage = "Unable to download resource " + resourceUrl + " ";

    switch (httpStatus) {
      case UNAUTHORIZED:
        errorMessage += "Login to " + serverUrl + " with credentials "
            + Base64.decodeBase64(basicAuthToken.getBytes()) + " failed";
        break;
      case NOT_FOUND:
        errorMessage += "Resource not found on the H2O server";
        break;
      default:
        errorMessage += "Server response status: " + httpStatus;
        break;
    }
    return errorMessage;
  }

}
