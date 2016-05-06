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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

public class UsernameValidationRuleTest {
  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void validate_allDataValid_doesntThrowException() {
    // given
    MultiValueMap<String, String> requestWithUsername = new LinkedMultiValueMap<>();
    requestWithUsername.add(UsernameValidationRule.USERNAME_KEY, "some-username");
    UsernameValidationRule rule = new UsernameValidationRule(new FormDataValidator());

    // when
    rule.validate(requestWithUsername);

    // then
    // no exception is thrown
  }

  @Test
  public void validate_noUsernameKeyInData_exceptionThrown() {
    // given
    MultiValueMap<String, String> requestWithoutUsernameKey = new LinkedMultiValueMap<>();
    UsernameValidationRule rule = new UsernameValidationRule(new FormDataValidator());

    // then
    thrown.expect(ValidationException.class);

    // when
    rule.validate(requestWithoutUsernameKey);
  }

  @Test
  public void validate_multipleUsernameKeysInData_exceptionThrown() {
    // given
    MultiValueMap<String, String> requestWithMultipleUsernameKeys = new LinkedMultiValueMap<>();
    requestWithMultipleUsernameKeys.add(UsernameValidationRule.USERNAME_KEY, "some-username");
    requestWithMultipleUsernameKeys.add(UsernameValidationRule.USERNAME_KEY, "other-username");
    UsernameValidationRule rule = new UsernameValidationRule(new FormDataValidator());

    // then
    thrown.expect(ValidationException.class);

    // when
    rule.validate(requestWithMultipleUsernameKeys);
  }

  @Test
  public void validate_emptyUsernameKeyInData_exceptionThrown() {
    // given
    MultiValueMap<String, String> requestWithEmptyUsernameKey = new LinkedMultiValueMap<>();
    requestWithEmptyUsernameKey.add(UsernameValidationRule.USERNAME_KEY, "");
    UsernameValidationRule rule = new UsernameValidationRule(new FormDataValidator());

    // then
    thrown.expect(ValidationException.class);

    // when
    rule.validate(requestWithEmptyUsernameKey);
  }
}
