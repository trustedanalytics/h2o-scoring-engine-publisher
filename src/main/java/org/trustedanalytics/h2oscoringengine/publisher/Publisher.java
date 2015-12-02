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
package org.trustedanalytics.h2oscoringengine.publisher;

import java.io.IOException;
import java.nio.file.Path;

import org.springframework.web.client.RestTemplate;
import org.trustedanalytics.h2oscoringengine.publisher.filesystem.FsDirectoryOperations;
import org.trustedanalytics.h2oscoringengine.publisher.filesystem.PublisherWorkingDirectory;
import org.trustedanalytics.h2oscoringengine.publisher.http.BasicAuthServerCredentials;
import org.trustedanalytics.h2oscoringengine.publisher.restapi.PublishRequest;
import org.trustedanalytics.h2oscoringengine.publisher.steps.AppRecordCreatingStep;
import org.trustedanalytics.h2oscoringengine.publisher.steps.CheckingIfAppExistsStep;
import org.trustedanalytics.h2oscoringengine.publisher.steps.H2oResourcesDownloadingStep;

public class Publisher {

  private final RestTemplate restTemplate;
  private final String cfApiUrl;
  private final BasicAuthServerCredentials appBroker;

  public Publisher(RestTemplate restTemplate, String cfApiUrl, BasicAuthServerCredentials appBroker)
      throws EnginePublicationException {
    this.restTemplate = restTemplate;
    this.cfApiUrl = cfApiUrl;
    this.appBroker = appBroker;
  }

  public void publish(PublishRequest request, String technicalSpaceGuid)
      throws EnginePublicationException, EngineBuildingException {

    String appName = request.getModelName();
    CheckingIfAppExistsStep appExistsStep = new CheckingIfAppExistsStep(cfApiUrl, restTemplate);
    if (appExistsStep.check(appName, technicalSpaceGuid)) {
      throw new EnginePublicationException(
          "Cannot publish app " + appName + " to CloudFoundry. App already exists.");
    }

    PublisherWorkingDirectory workingDir;
    try {
      workingDir =
          new PublisherWorkingDirectory(request.getModelName(), new FsDirectoryOperations());
    } catch (IOException e) {
      throw new EnginePublicationException("Unable to create dir for publisher: ", e);
    }

    Path scoringEngineJar =
        buildScoringEngineJar(workingDir, request.getH2oCredentials(), request.getModelName());

    publishToMarketplace(scoringEngineJar, appName, technicalSpaceGuid, request.getOrgGuid());

  }

  private Path buildScoringEngineJar(PublisherWorkingDirectory workingDir,
      BasicAuthServerCredentials h2oCredentials, String modelName)
      throws EnginePublicationException, EngineBuildingException {

    H2oResourcesDownloadingStep h2oResourcesDownloadingStep = new H2oResourcesDownloadingStep();
    Path scoringEngineJarPath = h2oResourcesDownloadingStep
        .downloadResources(h2oCredentials, modelName, workingDir.getH2oResourcesPath())
        .compileModel(workingDir.getCompiledModelPath()).packageModel(workingDir.getModelJarPath())
        .buildScoringEngine(workingDir.getScoringEngineJarDir());

    return scoringEngineJarPath;
  }

  private void publishToMarketplace(Path appBits, String appName, String technicalSpaceGuid,
      String orgGuid) throws EnginePublicationException {

    AppRecordCreatingStep appRecordCreatingStep = new AppRecordCreatingStep(cfApiUrl, restTemplate);
    appRecordCreatingStep.createAppRecord(technicalSpaceGuid, appName)
        .createAppRoute(technicalSpaceGuid, appName).uploadBits(appBits)
        .register(appBroker, appName, "Scoring engine based on H2O model")
        .addServicePlanVisibility(orgGuid, appName);
  }
}
