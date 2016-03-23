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
import org.trustedanalytics.h2oscoringengine.publisher.http.FilesDownloader;
import org.trustedanalytics.h2oscoringengine.publisher.restapi.PublishRequest;
import org.trustedanalytics.h2oscoringengine.publisher.steps.AppRecordCreatingStep;
import org.trustedanalytics.h2oscoringengine.publisher.steps.CheckingIfAppExistsStep;
import org.trustedanalytics.h2oscoringengine.publisher.steps.H2oResourcesDownloadingStep;

public class Publisher {

  private final RestTemplate cfRestTemplate;
  private final RestTemplate h2oServerRestTemplate;
  private final RestTemplate appBrokerRestTemplate;
  private final String cfApiUrl;
  private final BasicAuthServerCredentials appBroker;
  private final String engineBaseResourcePath;
  private final String technicalSpaceGuid;

  public Publisher(RestTemplate restTemplate, RestTemplate h2oServerRestTemplate, RestTemplate appBrokerRestTemplate, String cfApiUrl,
      BasicAuthServerCredentials appBroker, String engineBaseJar, String technicalSpaceGuid)
      throws EnginePublicationException {
    this.cfRestTemplate = restTemplate;
    this.cfApiUrl = cfApiUrl;
    this.appBroker = appBroker;
    this.engineBaseResourcePath = engineBaseJar;
    this.h2oServerRestTemplate = h2oServerRestTemplate;
    this.appBrokerRestTemplate = appBrokerRestTemplate;
    this.technicalSpaceGuid = technicalSpaceGuid;
  }

  public void publish(PublishRequest request)
      throws EnginePublicationException, EngineBuildingException {

    String appName = request.getModelName();
    CheckingIfAppExistsStep appExistsStep = new CheckingIfAppExistsStep(cfApiUrl, cfRestTemplate);
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

    Path scoringEngineJar = buildScoringEngineJar(workingDir,
        new FilesDownloader(request.getH2oCredentials(), h2oServerRestTemplate),
        request.getModelName());

    publishToMarketplace(scoringEngineJar, appName, technicalSpaceGuid, request.getOrgGuid());

  }

  private Path buildScoringEngineJar(PublisherWorkingDirectory workingDir,
      FilesDownloader h2oFilesDownloader, String modelName)
      throws EnginePublicationException, EngineBuildingException {

    H2oResourcesDownloadingStep h2oResourcesDownloadingStep = new H2oResourcesDownloadingStep();
    return h2oResourcesDownloadingStep
        .downloadResources(h2oFilesDownloader, modelName, workingDir.getH2oResourcesPath())
        .compileModel(workingDir.getCompiledModelPath()).packageModel(workingDir.getModelJarPath())
        .buildScoringEngine(workingDir.getScoringEngineJarDir(), engineBaseResourcePath);
  }

  private void publishToMarketplace(Path appBits, String appName, String technicalSpaceGuid,
      String orgGuid) throws EnginePublicationException {

    AppRecordCreatingStep appRecordCreatingStep =
        new AppRecordCreatingStep(cfApiUrl, cfRestTemplate);
    appRecordCreatingStep.createAppRecord(technicalSpaceGuid, appName)
        .createAppRoute(technicalSpaceGuid, appName).uploadBits(appBits)
        .register(appBroker, appBrokerRestTemplate, appName, "Scoring engine based on H2O model")
        .addServicePlanVisibility(orgGuid, appName);
  }
}
