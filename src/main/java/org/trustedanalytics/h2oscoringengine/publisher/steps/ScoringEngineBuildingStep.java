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
package org.trustedanalytics.h2oscoringengine.publisher.steps;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trustedanalytics.h2oscoringengine.publisher.EngineBuildingException;
import org.trustedanalytics.h2oscoringengine.publisher.filesystem.UpdatableJar;

public class ScoringEngineBuildingStep {

  private static final Logger LOGGER = LoggerFactory.getLogger(ScoringEngineBuildingStep.class);
  private static final String SCORING_ENGINE_FILE_NAME = "scoring-engine.jar";
  private static final String ENGINE_BASE_RESOURCE_PATH =
      "/runtime/h2o-scoring-engine-base-0.4.10.jar";
  private static final String ENGINE_BASE_JAR_NAME = "scoring-engine-base.jar";

  private final Path modelJarPath;

  public ScoringEngineBuildingStep(Path modelJarPath) {
    this.modelJarPath = modelJarPath;
  }

  public Path buildScoringEngine(Path scoringEngineDir) throws EngineBuildingException {
    try {
      LOGGER.info("Creating scoring engine JAR for model: " + modelJarPath);
      Path scoringEngineJar = buildScoringEngine(modelJarPath, scoringEngineDir);
      LOGGER.info("Generated JAR: " + scoringEngineJar);
      return scoringEngineJar;
    } catch (IOException e) {
      LOGGER.error("Error while creating scoring engine JAR: ", e);
      throw new EngineBuildingException("Error while creating scoring engine JAR: ", e);
    }
  }

  private Path buildScoringEngine(Path modelJarPath, Path targetDir) throws IOException {
    UpdatableJar engineJar = new UpdatableJar(SCORING_ENGINE_FILE_NAME, targetDir);
    Path engineJarPath =
        engineJar.addJarContent(createTemplateJar(ENGINE_BASE_RESOURCE_PATH, targetDir))
            .addUncompressedLibJar(modelJarPath).getJarPath();
    engineJar.close();
    return engineJarPath;
  }

  private JarFile createTemplateJar(String baseJarResourcePath, Path targetDir)
      throws IOException {
    URL resource = this.getClass().getResource(baseJarResourcePath);
    if (null == resource) {
      throw new IOException("JAR resource " + baseJarResourcePath + " not found");
    }

    Path templateJar = targetDir.resolve(ENGINE_BASE_JAR_NAME);
    InputStream templateResource = this.getClass().getResourceAsStream(baseJarResourcePath);
    Files.copy(templateResource, templateJar);
    return new JarFile(templateJar.toFile());
  }

}
