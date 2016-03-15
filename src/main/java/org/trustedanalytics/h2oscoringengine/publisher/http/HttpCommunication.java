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

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;

public final class HttpCommunication {
  
  private HttpCommunication(){}

  public static HttpEntity<String> simpleJsonRequest() {
    return new HttpEntity<>(createJsonHeaders());
  }

  public static HttpEntity<String> postRequest(String body) {
    HttpHeaders headers = new HttpHeaders();
    headers.add("Accept", "application/json");
    headers.add("Content-type", "application/x-www-form-urlencoded");

    return new HttpEntity<>(body, headers);
  }

  public static HttpEntity<String> basicAuthRequest(String basicAuthToken) {
    HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Basic " + basicAuthToken);

    return new HttpEntity<>(headers);
  }
  
  public static HttpHeaders basicAuthJsonHeaders(String basicAuthToken) {
    HttpHeaders headers = new HttpHeaders();
    headers.add("Content-type", "application/json");
    headers.add("Authorization", "Basic " + basicAuthToken);

    return headers;
  }
  
  public static HttpHeaders zipHeaders() {
    HttpHeaders bitsHeaders = new HttpHeaders();
    bitsHeaders.add("Content-type", "application/zip");

    return bitsHeaders;
  }

  private static HttpHeaders createJsonHeaders() {
    HttpHeaders headers = new HttpHeaders();
    headers.add("Accept", "application/json");
    headers.add("Content-type", "application/json");

    return headers;
  }
  
}
