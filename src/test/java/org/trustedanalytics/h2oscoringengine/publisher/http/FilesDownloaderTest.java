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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.codec.binary.Base64;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

public class FilesDownloaderTest {

  private final BasicAuthServerCredentials testCredentials =
      new BasicAuthServerCredentials("somehost", "username", "password");
  private final String testResource = "/skjfks/asf";
  private Path testPath;
  private String testServerResponse = "Some server response";
  private final RestTemplate restTemplateMock = mock(RestTemplate.class);
  private final ResponseEntity<byte[]> responseMock = mock(ResponseEntity.class);

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Before
  public void setUp() throws IOException {
    this.testPath = Files.createTempFile("some", "file");

    when(restTemplateMock.exchange(testCredentials.getHost() + testResource, HttpMethod.GET,
        HttpCommunication.basicAuthRequest(testCredentials.getBasicAuthToken()), byte[].class))
            .thenReturn(responseMock);
  }

  @Test
  public void download_serverResponse200_resourcesDownloaded() throws Exception {
    // given
    FilesDownloader downloader = new FilesDownloader(testCredentials, restTemplateMock);

    // when
    when(responseMock.getBody()).thenReturn(testServerResponse.getBytes());
    downloader.download(testResource, testPath);

    // then
    verify(restTemplateMock).exchange(testCredentials.getHost() + testResource, HttpMethod.GET,
        HttpCommunication.basicAuthRequest(testCredentials.getBasicAuthToken()), byte[].class);
    assertThat(new String(Files.readAllBytes(testPath)), equalTo(testServerResponse));
  }

  @Test
  public void download_serverResponse401_excpetionWithProperMessageThrown() throws IOException {
    // given
    FilesDownloader downloader = new FilesDownloader(testCredentials, restTemplateMock);
    String expectedExceptionMessage = "Login to " + testCredentials.getHost() + " with credentials "
        + new String(Base64.decodeBase64(testCredentials.getBasicAuthToken().getBytes())) + " failed";

    // when
    when(restTemplateMock.exchange(testCredentials.getHost() + testResource, HttpMethod.GET,
        HttpCommunication.basicAuthRequest(testCredentials.getBasicAuthToken()), byte[].class))
            .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));

    // then
    thrown.expect(IOException.class);
    thrown.expectMessage(expectedExceptionMessage);
    downloader.download(testResource, testPath);
  }
  
  @Test
  public void download_serverResponse404_excpetionWithProperMessageThrown() throws IOException {
    // given
    FilesDownloader downloader = new FilesDownloader(testCredentials, restTemplateMock);
    String expectedExceptionMessage = "Resource not found.";

    // when
    when(restTemplateMock.exchange(testCredentials.getHost() + testResource, HttpMethod.GET,
        HttpCommunication.basicAuthRequest(testCredentials.getBasicAuthToken()), byte[].class))
            .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

    // then
    thrown.expect(IOException.class);
    thrown.expectMessage(expectedExceptionMessage);
    downloader.download(testResource, testPath);
  }
  
  @Test
  public void download_serverResponseErrorOtherThan401And404_excpetionWithProperMessageThrown() throws IOException {
    // given
    FilesDownloader downloader = new FilesDownloader(testCredentials, restTemplateMock);
    HttpStatus expectedErrorStatus = HttpStatus.BAD_GATEWAY;
    String expectedExceptionMessage = "Server response status: " + expectedErrorStatus;

    // when
    when(restTemplateMock.exchange(testCredentials.getHost() + testResource, HttpMethod.GET,
        HttpCommunication.basicAuthRequest(testCredentials.getBasicAuthToken()), byte[].class))
            .thenThrow(new HttpClientErrorException(expectedErrorStatus));

    // then
    thrown.expect(IOException.class);
    thrown.expectMessage(expectedExceptionMessage);
    downloader.download(testResource, testPath);
  }

}
