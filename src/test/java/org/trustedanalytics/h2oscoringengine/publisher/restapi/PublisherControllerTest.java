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
package org.trustedanalytics.h2oscoringengine.publisher.restapi;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Paths;

import org.junit.Before;
import org.junit.Test;
import org.trustedanalytics.h2oscoringengine.publisher.EngineBuildingException;
import org.trustedanalytics.h2oscoringengine.publisher.Publisher;
import org.trustedanalytics.h2oscoringengine.publisher.http.BasicAuthServerCredentials;

public class PublisherControllerTest {

  private Publisher publisherMock = mock(Publisher.class);
  private PublishRequest testPublishRequest;
  private DownloadRequest testDownloadRequest;

  @Before
  public void setUp() throws EngineBuildingException {
    testPublishRequest = new PublishRequest();
    testPublishRequest
        .setH2oCredentials(new BasicAuthServerCredentials("host", "username", "password"));
    testPublishRequest.setModelName("some-model-name");
    testPublishRequest.setOrgGuid("some-org-guid");

    testDownloadRequest = new DownloadRequest();
    testDownloadRequest
        .setH2oCredentials(new BasicAuthServerCredentials("host", "username", "password"));
    testDownloadRequest.setModelName("some-model-name");
    
  }

  @Test
  public void publish_callsPublisher() throws Exception {
    // given
    PublisherController controller = new PublisherController(publisherMock);

    // when
    controller.publish(testPublishRequest);

    // then
    verify(publisherMock).publish(testPublishRequest);
  }

  @Test
  public void downloadEngine_callsPublisher() throws Exception {
    // given
    PublisherController controller = new PublisherController(publisherMock);

    // when
    when(publisherMock.getScoringEngineJar(testDownloadRequest)).thenReturn(Paths.get("/tmp/"));
    controller.downloadEngine(testDownloadRequest);

    // then
    verify(publisherMock).getScoringEngineJar(testDownloadRequest);
  }
}
