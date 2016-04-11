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

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.trustedanalytics.h2oscoringengine.publisher.EngineBuildingException;
import org.trustedanalytics.h2oscoringengine.publisher.EnginePublicationException;
import org.trustedanalytics.h2oscoringengine.publisher.Publisher;

@RestController
public class PublisherController {

  private static final Logger LOGGER = LoggerFactory.getLogger(PublisherController.class);

  private final Publisher publisher;

  @Autowired
  public PublisherController(Publisher publisher) {
    this.publisher = publisher;
  }

  @RequestMapping(method = RequestMethod.POST, consumes = "application/json", value = "/rest/engine")
  public void publish(@Valid @RequestBody PublishRequest publishRequest)
      throws EnginePublicationException, EngineBuildingException {
    LOGGER.info("Got publish request: " + publishRequest.toString());
    publisher.publish(publishRequest);
  }

}
