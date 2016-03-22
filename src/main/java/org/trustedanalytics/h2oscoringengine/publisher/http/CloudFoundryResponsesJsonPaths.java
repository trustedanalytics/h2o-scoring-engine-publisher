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

public final class CloudFoundryResponsesJsonPaths {

  private static final String GUID_PATH = "/resources/0/metadata/guid";
  
  public static final String APPS_NUMBER_JSON_PATH = "/total_results";
  public static final String DOMAIN_JSON_PATH = GUID_PATH;
  public static final String ROUTE_JSON_PATH = GUID_PATH;
  public static final String ROUTES_NUMBER_JSON_PATH = "/total_results";
  public static final String ROUTE_GUID_JSON_PATH = "/metadata/guid";
  public static final String APP_GUID_JSON_PATH = "/metadata/guid";
  public static final String SERVICE_GUID_JSON_PATH = GUID_PATH;
  public static final String FIRST_SERVICE_PLAN_GUID = GUID_PATH;
  

  private CloudFoundryResponsesJsonPaths() {
  }
  
}
