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
import static org.hamcrest.Matchers.is;

import org.apache.commons.codec.binary.Base64;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;

public class HttpCommunicationTest {

  private String basicAuthToken;

  @Before
  public void setUo() {
    String basicAuthCredentials = "kasjfk:arhgjk";
    this.basicAuthToken = new String(Base64.encodeBase64(basicAuthCredentials.getBytes()));
  }

  @Test
  public void simpleJsonRequest_createsProperRequest() throws Exception {
    // when
    HttpEntity<String> jsonRequest = HttpCommunication.simpleJsonRequest();

    // then
    assertThat(jsonRequest, is(equalTo(createExpectedSimpleJsonRequest())));
  }

  @Test
  public void postRequest() throws Exception {
    // given
    String postBody = "klasjghakhg";

    // when
    HttpEntity<String> postRequest = HttpCommunication.postRequest(postBody);

    // then
    assertThat(postRequest, is(equalTo(createExpectedPostRequest(postBody))));
  }

  @Test
  public void basicAuthRequest() throws Exception {
    // when
    HttpEntity<String> request = HttpCommunication.basicAuthRequest(basicAuthToken);

    // then
    assertThat(request, is(equalTo(createExpectedBasicAuthRequest(basicAuthToken))));

  }

  @Test
  public void basicAuthJsonHeaders() throws Exception {
    // when
    HttpHeaders headers = HttpCommunication.basicAuthJsonHeaders(basicAuthToken);

    // then
    assertThat(headers, is(equalTo(createExpectedBasicAuthJsonHeaders(basicAuthToken))));

  }

  @Test
  public void zipHeaders() throws Exception {
    // when
    HttpHeaders headers = HttpCommunication.zipHeaders();

    // then
    assertThat(headers, is(equalTo(createExpectedZipHeaders())));
  }

  private HttpEntity<String> createExpectedSimpleJsonRequest() {
    HttpHeaders headers = new HttpHeaders();
    headers.add("Accept", "application/json");
    headers.add("Content-type", "application/json");

    return new HttpEntity<>(headers);
  }

  private HttpEntity<String> createExpectedPostRequest(String body) {
    HttpHeaders headers = new HttpHeaders();
    headers.add("Accept", "application/json");
    headers.add("Content-type", "application/x-www-form-urlencoded");

    return new HttpEntity<>(body, headers);
  }

  private HttpEntity<String> createExpectedBasicAuthRequest(String basicAuthToken) {
    HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Basic " + basicAuthToken);

    return new HttpEntity<>(headers);
  }

  private HttpHeaders createExpectedBasicAuthJsonHeaders(String basicAuthToken) {
    HttpHeaders headers = new HttpHeaders();
    headers.add("Content-type", "application/json");
    headers.add("Authorization", "Basic " + basicAuthToken);

    return headers;
  }

  private HttpHeaders createExpectedZipHeaders() {
    HttpHeaders bitsHeaders = new HttpHeaders();
    bitsHeaders.add("Content-type", "application/zip");

    return bitsHeaders;
  }

}
