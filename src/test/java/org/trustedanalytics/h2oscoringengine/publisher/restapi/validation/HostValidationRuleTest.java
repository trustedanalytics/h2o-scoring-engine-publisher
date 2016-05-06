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

public class HostValidationRuleTest {
  
  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void validate_allDataValid_doesntThrowException() {
    //given
    MultiValueMap<String, String> requestWithHost = new LinkedMultiValueMap<>();
    requestWithHost.add(HostValidationRule.HOST_KEY, "http://example.com");
    HostValidationRule rule = new HostValidationRule(new FormDataValidator());
    
    //when
    rule.validate(requestWithHost);
    
    //then
    // no exception is thrown
  }
  
  @Test
  public void validate_noHostKeyInData_exceptionThrown() {
    //given
    MultiValueMap<String, String> requestWithoutHostKey = new LinkedMultiValueMap<>();
    HostValidationRule rule = new HostValidationRule(new FormDataValidator());
    
    //then
    thrown.expect(ValidationException.class);
    
    //when
    rule.validate(requestWithoutHostKey);
  }
  
  @Test
  public void validate_multipleHostKeysInData_exceptionThrown() {
    //given
    MultiValueMap<String, String> requestWithMultipleHostKeys = new LinkedMultiValueMap<>();
    requestWithMultipleHostKeys.add(HostValidationRule.HOST_KEY, "http://example.com");
    requestWithMultipleHostKeys.add(HostValidationRule.HOST_KEY, "http://example.com");
    HostValidationRule rule = new HostValidationRule(new FormDataValidator());
    
    //then
    thrown.expect(ValidationException.class);
    
    //when
    rule.validate(requestWithMultipleHostKeys);
  }
  
  @Test
  public void validate_emptyHostKeyInData_exceptionThrown() {
    //given
    MultiValueMap<String, String> requestWithEmptyHostKey = new LinkedMultiValueMap<>();
    requestWithEmptyHostKey.add(HostValidationRule.HOST_KEY, "");
    HostValidationRule rule = new HostValidationRule(new FormDataValidator());
    
    //then
    thrown.expect(ValidationException.class);
    
    //when
    rule.validate(requestWithEmptyHostKey);
  }
  
  @Test
  public void validate_invalidUrlInHostKeyInData_exceptionThrown() {
    //given
    MultiValueMap<String, String> requestWithInvalidUrl = new LinkedMultiValueMap<>();
    requestWithInvalidUrl.add(HostValidationRule.HOST_KEY, "lsakjfajkadf");
    HostValidationRule rule = new HostValidationRule(new FormDataValidator());
    
    //then
    thrown.expect(ValidationException.class);
    
    //when
    rule.validate(requestWithInvalidUrl);
  }

}
