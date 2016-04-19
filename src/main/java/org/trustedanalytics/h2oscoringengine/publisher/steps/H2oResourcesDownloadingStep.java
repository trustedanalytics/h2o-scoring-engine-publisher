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
import java.nio.file.Path;

import org.trustedanalytics.h2oscoringengine.publisher.EngineBuildingException;
import org.trustedanalytics.h2oscoringengine.publisher.http.FilesDownloader;

public class H2oResourcesDownloadingStep {

  public static final String H2O_SERVER_MODEL_PATH_PREFIX = "/3/Models.java/";
  public static final String H2O_SERVER_LIB_PATH = "/3/h2o-genmodel.jar";
  public static final String H2O_LIB_FILE_NAME = "genmodel.jar";

  public ModelCompilationStep downloadResources(FilesDownloader h2oFilesDownloader,
      String modelName, Path targetDirectory) throws EngineBuildingException {
    Path fileForModelPojo = targetDirectory.resolve(getModelPojoFileName(modelName));
    Path fileForLib = targetDirectory.resolve(H2O_LIB_FILE_NAME);

    try {
      Path downloadedModelPath =
          h2oFilesDownloader.download(H2O_SERVER_MODEL_PATH_PREFIX + modelName, fileForModelPojo);
      Path downloadedLibPath = h2oFilesDownloader.download(H2O_SERVER_LIB_PATH, fileForLib);
      return new ModelCompilationStep(downloadedModelPath, downloadedLibPath);

    } catch (IOException e) {
      throw new EngineBuildingException(
          "Unable to download resources for scoring engine: " + e.getMessage(), e);
    }
  }

  /**
   * *.java file name has to be equal to class name it contains. For models with dash in name H2O
   * generates class name with dash replaced by underscore. For others - class name is the same as
   * model name.
   * 
   * @param modelName
   * @return
   */
  private String getModelPojoFileName(String modelName) {
    return modelName.replace("-", "_") + ".java";
  }

}
