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

import org.springframework.util.MultiValueMap;

public class UsernameValidationRule implements DownloadRequestValidationRule {

  static final String USERNAME_KEY = "username";
  private final FormDataValidator formDataValidator;

  public UsernameValidationRule(FormDataValidator formDataValidator) {
    this.formDataValidator = formDataValidator;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.trustedanalytics.h2oscoringengine.publisher.restapi.DownloadRequestValidationRule#validate(
   * org.springframework.util.MultiValueMap)
   */
  @Override
  public void validate(MultiValueMap<String, String> request) throws ValidationException {
    try {
      formDataValidator.validateField(request, USERNAME_KEY);
    } catch (IllegalArgumentException e) {
      throw new ValidationException(e);
    }
  }
}
