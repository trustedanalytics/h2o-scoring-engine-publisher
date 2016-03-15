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

import static org.mockito.Mockito.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import org.apache.commons.codec.binary.Base64;
import org.junit.Before;
import org.junit.Test;

public class BasicAuthServerCredentialsTest {

  private String host = "sakdfajkshfl";
  private String username = "wpieu";
  private String password = "ljkadh";
  private String expectedBasicAuthToken;

  @Before
  public void setUp() {
    String basicAuthString = username + ":" + password;
    this.expectedBasicAuthToken = new String(Base64.encodeBase64(basicAuthString.getBytes()));
  }

  @Test
  public void getBasicAuthToken_returnsProperBasicAuthToken() throws Exception {
    // given
    BasicAuthServerCredentials credentials =
        new BasicAuthServerCredentials(host, username, password);

    // when
    String basicAuthToken = credentials.getBasicAuthToken();

    // then
    assertThat(basicAuthToken, is(equalTo(expectedBasicAuthToken)));
  }

}
