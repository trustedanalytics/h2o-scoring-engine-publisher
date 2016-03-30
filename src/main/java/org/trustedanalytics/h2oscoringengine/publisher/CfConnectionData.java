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

import org.springframework.web.client.RestTemplate;

public class CfConnectionData {
  
  private final RestTemplate cfRestTemplate;
  private final String cfApiUrl;
  private final String technicalSpaceGuid;
  
  public CfConnectionData(RestTemplate cfRestTemplate, String cfApiUrl, String technicalSpaceGuid) {
    this.cfRestTemplate = cfRestTemplate;
    this.cfApiUrl = cfApiUrl;
    this.technicalSpaceGuid = technicalSpaceGuid;
  }
  
  /**
   * @return the cfRestTemplate
   */
  public RestTemplate getCfRestTemplate() {
    return cfRestTemplate;
  }

  /**
   * @return the cfApiUrl
   */
  public String getCfApiUrl() {
    return cfApiUrl;
  }

  /**
   * @return the technicalSpaceGuid
   */
  public String getTechnicalSpaceGuid() {
    return technicalSpaceGuid;
  }

}
