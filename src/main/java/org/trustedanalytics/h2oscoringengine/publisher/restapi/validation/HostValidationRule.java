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
package org.trustedanalytics.h2oscoringengine.publisher.restapi.validation;

import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.util.MultiValueMap;

public class HostValidationRule implements DownloadRequestValidationRule {

  static final String HOST_KEY = "host";
  private final FormDataValidator formDataValidator;

  public HostValidationRule(FormDataValidator formDataValidator) {
    this.formDataValidator = formDataValidator;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.trustedanalytics.h2oscoringengine.publisher.restapi.DowloadRequestValidationRule#validate(
   * org.springframework.util.MultiValueMap)
   */
  @Override
  public void validate(MultiValueMap<String, String> request) {
    try {
      formDataValidator.validateField(request, HOST_KEY);
      UrlValidator urlValidator = new UrlValidator();
      if (!urlValidator.isValid(request.get(HOST_KEY).get(0))) {
        throw new ValidationException("Url given in " + HOST_KEY + " form field is invalid");
      }

    } catch (IllegalArgumentException e) {
      throw new ValidationException(e);
    }
  }

}
