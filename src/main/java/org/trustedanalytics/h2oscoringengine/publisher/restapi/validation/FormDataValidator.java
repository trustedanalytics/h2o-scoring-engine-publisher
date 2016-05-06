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

import com.google.common.base.Preconditions;

class FormDataValidator {

  public FormDataValidator() {}

  public void validateField(MultiValueMap<String, String> formData, String fieldKey) {
    Preconditions.checkArgument(formData.containsKey(fieldKey), "There's no '%s' key in form data.",
        fieldKey);
    Preconditions.checkArgument(1 == formData.get(fieldKey).size(),
        "There's %s '%s' keys in form data. There should be exactly 1.",
        formData.get(fieldKey).size(), fieldKey);
    Preconditions.checkArgument(!formData.get(fieldKey).get(0).isEmpty(),
        "'%s' key in form data is empty.", fieldKey);
  }
}
