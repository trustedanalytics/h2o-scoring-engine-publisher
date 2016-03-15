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
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * Downloads files from BasicAuth secured REST server.
 */
public class FilesDownloader {

  private static final Logger LOGGER = LoggerFactory.getLogger(FilesDownloader.class);

  private final String basicAuthToken;
  private final String serverUrl;
  private final RestTemplate basicAuthRestTemplate;

  public FilesDownloader(BasicAuthServerCredentials serverCredentials,
      RestTemplate basicAuthRestTemplate) {
    this.serverUrl = serverCredentials.getHost();
    this.basicAuthToken = serverCredentials.getBasicAuthToken();
    this.basicAuthRestTemplate = basicAuthRestTemplate;
  }

  public Path download(String resourcePath, Path destinationFilePath) throws IOException {

    basicAuthRestTemplate.getMessageConverters().add(new ByteArrayHttpMessageConverter());

    String resourceUrl = serverUrl + resourcePath;

    LOGGER.info("Downloading " + resourceUrl);

    try {
      ResponseEntity<byte[]> response = basicAuthRestTemplate.exchange(resourceUrl, HttpMethod.GET,
          HttpCommunication.basicAuthRequest(basicAuthToken), byte[].class);
      return Files.write(destinationFilePath, response.getBody());

    } catch (HttpClientErrorException e) {
      String errorMessage = prepareErrorMessage(e.getStatusCode(), resourceUrl);
      LOGGER.error(errorMessage);
      throw new IOException(errorMessage);
    }
  }

  private String prepareErrorMessage(HttpStatus httpStatus, String resourceUrl) {
    String errorMessage = "Unable to download resource " + resourceUrl + " ";

    switch (httpStatus) {
      case UNAUTHORIZED:
        errorMessage += "Login to " + serverUrl + " with credentials "
            + new String(Base64.decodeBase64(basicAuthToken.getBytes())) + " failed";
        break;
      case NOT_FOUND:
        errorMessage += "Resource not found.";
        break;
      default:
        errorMessage += "Server response status: " + httpStatus;
        break;
    }
    return errorMessage;
  }

}
