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
package org.trustedanalytics.h2oscoringengine.publisher.filesystem;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PublisherWorkingDirectory {

  static final String SYSTEM_TMP = System.getProperty("java.io.tmpdir");
  static final String PUBLISHER_PARENT_DIRECTORY_NAME = "h2o-scoring-engine-publisher";
  static final String H2O_RESOURCES_SUBDIR_NAME = "model";
  static final String COMPILED_MODEL_SUBDIR_NAME = "classes";

  private String workingDirectoryName;
  private DirectoryOperations directoryOperations;

  private Path h2oResourcesPath;
  private Path compiledModelPath;
  private Path modelJarPath;
  private Path scoringEngineJarDir;

  public PublisherWorkingDirectory(String workingDirectoryName, DirectoryOperations dirOperations)
      throws IOException {
    this.workingDirectoryName = workingDirectoryName;
    this.directoryOperations = dirOperations;

    createDirectoryTree();
  }

  public Path getCompiledModelPath() {
    return compiledModelPath;
  }

  public Path getH2oResourcesPath() {
    return h2oResourcesPath;
  }

  public Path getModelJarPath() {
    return modelJarPath;
  }
  
  public Path getScoringEngineJarDir() {
    return scoringEngineJarDir;
  }
  
  private void createDirectoryTree() throws IOException {
    Path workingDir = Paths.get(SYSTEM_TMP, PUBLISHER_PARENT_DIRECTORY_NAME, workingDirectoryName);
    directoryOperations.createEmptyDirectoryTree(workingDir);

    this.h2oResourcesPath =
        directoryOperations.createSubdirectory(workingDir, H2O_RESOURCES_SUBDIR_NAME);
    this.compiledModelPath =
        directoryOperations.createSubdirectory(workingDir, COMPILED_MODEL_SUBDIR_NAME);
    this.modelJarPath = workingDir;
    this.scoringEngineJarDir = workingDir;
  }
}
