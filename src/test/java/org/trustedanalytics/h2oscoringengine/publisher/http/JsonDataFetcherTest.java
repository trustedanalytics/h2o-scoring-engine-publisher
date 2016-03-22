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

import org.junit.Before;
import org.junit.Test;

public class JsonDataFetcherTest {

  private String testJson;
  private String testStringValue = "sakjdfjk";
  private String testIntValue = "987";

  @Before
  public void setUp() {
    this.testJson = "{\"app\" : {\"metadata\" : {\"guid\" : \"aaa\"}},\"id\":\"" + testIntValue
        + "\",\"plans\" : [{\"id\" : \"" + testStringValue
        + "\"}],\"description\" : \"Scoring engine based on H2O model\",\"name\" : \"serviceName\"}";
  }

  @Test
  public void getStringValue_validJsonGiven_returnsProperValue() throws Exception {
    // given
    String valuePath = "/plans/0/id";

    // when
    String value = JsonDataFetcher.getStringValue(testJson, valuePath);
    
    //then
    assertThat(value, is(equalTo(testStringValue)));
  }

  @Test
  public void getIntValue() throws Exception {
    // given
    String valuePath = "/id";

    // when
    Integer value = JsonDataFetcher.getIntValue(testJson, valuePath);
    
    //then
    assertThat(value, is(equalTo(Integer.parseInt(testIntValue))));

  }

}
