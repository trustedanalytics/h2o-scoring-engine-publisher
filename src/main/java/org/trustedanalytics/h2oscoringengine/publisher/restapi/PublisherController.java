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

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.trustedanalytics.h2oscoringengine.publisher.EngineBuildingException;
import org.trustedanalytics.h2oscoringengine.publisher.EnginePublicationException;
import org.trustedanalytics.h2oscoringengine.publisher.Publisher;
import org.trustedanalytics.h2oscoringengine.publisher.http.BasicAuthServerCredentials;
import org.trustedanalytics.h2oscoringengine.publisher.restapi.validation.DownloadRequestValidationRule;
import org.trustedanalytics.h2oscoringengine.publisher.restapi.validation.DownloadRequestValidationRules;
import org.trustedanalytics.h2oscoringengine.publisher.restapi.validation.ValidationException;

import javax.validation.Valid;
import java.util.List;


@RestController
public class PublisherController {

  private static final Logger LOGGER = LoggerFactory.getLogger(PublisherController.class);

  private final Publisher publisher;
  private final List<DownloadRequestValidationRule> validationRules;

  @Autowired
  public PublisherController(Publisher publisher,
      DownloadRequestValidationRules downloadRequestValidationRules) {
    this.publisher = publisher;
    this.validationRules = downloadRequestValidationRules.get();
  }

  @ApiOperation(
          value = "Publishes model as a service offering in Marketplace",
          notes = "Privilege level: Any consumer of this endpoint must have a valid access token"
  )
  @ApiResponses(value = {
          @ApiResponse(code = 200, message = "OK"),
          @ApiResponse(code = 500, message = "Internal server error, e.g. error building or publishing model")
  })
  @RequestMapping(method = RequestMethod.POST, consumes = "application/json",
      value = "/rest/h2o/engines")
  public void publish(@Valid @RequestBody PublishRequest publishRequest)
      throws EnginePublicationException, EngineBuildingException {
    LOGGER.info("Got publish request: " + publishRequest);
    publisher.publish(publishRequest);
  }

  @ApiOperation(
          value = "Exposes H2O scoring engine model for download as JAR file",
          notes = "Privilege level: Any consumer of this endpoint must have a valid access token"
  )
  @ApiResponses(value = {
          @ApiResponse(code = 200, message = "OK", response = FileSystemResource.class),
          @ApiResponse(code = 400, message = "Request was malformed"),
          @ApiResponse(code = 500, message = "Internal server error, e.g. error building or publishing model")
  })
  @RequestMapping(method = RequestMethod.POST, consumes = "application/x-www-form-urlencoded",
      value = "/rest/h2o/engines/{modelName}/downloads", produces = "application/java-archive")
  @ResponseBody
  public FileSystemResource downloadEngine(
      @Valid @RequestBody MultiValueMap<String, String> request, @PathVariable String modelName)
      throws EngineBuildingException, ValidationException {

    LOGGER.info("Got download request: " + request + " modelName:" + modelName);
    validationRules.forEach(rule -> rule.validate(request));

    BasicAuthServerCredentials h2oServerCredentials = new BasicAuthServerCredentials(
        request.get("host").get(0), request.get("username").get(0), request.get("password").get(0));

    return new FileSystemResource(
        publisher.getScoringEngineJar(h2oServerCredentials, modelName).toFile());
  }

  /**
   * @deprecated This API endpoint will be removed.  
   */
  @ApiOperation(
          value = "Deprecated route: Publishes model as a service offering in Marketplace",
          notes = "Privilege level: Any consumer of this endpoint must have a valid access token"
  )
  @ApiResponses(value = {
          @ApiResponse(code = 200, message = "OK"),
          @ApiResponse(code = 500, message = "Internal server error, e.g. error building or publishing model")
  })
  @RequestMapping(method = RequestMethod.POST, consumes = "application/json",
      value = "/rest/engine")
  @Deprecated
  public void publishOldEndpoint(@Valid @RequestBody PublishRequest publishRequest)
      throws EnginePublicationException, EngineBuildingException {
    LOGGER.info("Got publish request: " + publishRequest);
    publisher.publish(publishRequest);
  }

  /**
   * @deprecated This API endpoint will be removed.  
   */
  @ApiOperation(
          value = "Deprecated route: Exposes H2O scoring engine model for download as JAR file",
          notes = "Privilege level: Any consumer of this endpoint must have a valid access token"
  )
  @ApiResponses(value = {
          @ApiResponse(code = 200, message = "OK"),
          @ApiResponse(code = 500, message = "Internal server error, e.g. error building model")
  })
  @RequestMapping(method = RequestMethod.POST, consumes = "application/json",
      value = "/rest/downloads", produces = "application/java-archive")
  @ResponseBody
  @Deprecated
  public FileSystemResource downloadEngine(@Valid @RequestBody DownloadRequest downloadRequest)
      throws EngineBuildingException {
    LOGGER.info("Got download request: " + downloadRequest);
    return new FileSystemResource(publisher
        .getScoringEngineJar(downloadRequest.getH2oCredentials(), downloadRequest.getModelName())
        .toFile());
  }

  @ExceptionHandler(ValidationException.class)
  @ResponseStatus(value = HttpStatus.BAD_REQUEST)
  @ResponseBody
  public String handleIllegalArgumentException(ValidationException e) {
    LOGGER.error("Invalid request - returning HTTP 400 response. Reason: ", e);
    return e.getMessage();
  }

  @ExceptionHandler({EngineBuildingException.class, EnginePublicationException.class})
  @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
  @ResponseBody
  public String handleEngineBuildingException(Exception e) {
    LOGGER.error("Problem while building/publishing scoring engine: ", e);
    return e.getMessage();
  }

}
