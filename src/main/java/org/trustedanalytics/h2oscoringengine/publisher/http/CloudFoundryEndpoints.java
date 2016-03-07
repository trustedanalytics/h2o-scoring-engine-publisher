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

public final class CloudFoundryEndpoints {

  private CloudFoundryEndpoints() {}

  public static final String APP_IN_SPACE_ENDPOINT_TEMPLATE =
      "/v2/spaces/{spaceGuid}/apps?q=name:{appName}";
  public static final String APPS_ENDPOINT = "/v2/apps";
  public static final String SHARED_DOMAINS_ENDPOINT = "/v2/shared_domains";
  public static final String ROUTES_ENDPOINT = "/v2/routes";
  public static final String GET_ROUTES_ENDPOINT_TEMPLATE =
      "/v2/routes?q=host:{name};domain_guid:{guid}";
  public static final String BIND_ROUTE_TO_APP_ENDPOINT_TEMPLATE =
      "/v2/apps/{appGuid}/routes/{routeGuid}";
  public static final String APP_BITS_ENDPOINT_TEMPLATE = "/v2/apps/{appGuid}/bits";
  public static final String APP_BROKER_CATALOG_ENDPOINT = "/v2/catalog";
  public static final String GET_SERVICE_GUID_BY_NAME_ENDPOINT_TEMPLATE =
      "/v2/services?q=label:{serviceName}";
  public static final String GET_SERVICE_PLANS_ENDPOINT_TEMPLATE =
      "/v2/service_plans?q=service_guid:{serviceGuid}";
  public static final String SERVICE_PLAN_VISIBILITIES_ENDPOINT = "/v2/service_plan_visibilities";
}
