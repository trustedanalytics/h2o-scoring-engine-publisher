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

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trustedanalytics.h2oscoringengine.publisher.EnginePublicationException;

public class ModelPackagingStep {

  private static final Logger LOGGER = LoggerFactory.getLogger(ModelPackagingStep.class);
  private static final String MODEL_JAR_FILE_NAME = "model.jar";

  private final Path classesDir;


  public ModelPackagingStep(Path classesDir) {
    this.classesDir = classesDir;
  }

  public ScoringEngineBuildingStep packageModel(Path targetDir) throws EnginePublicationException {
    Path fileForJar = targetDir.resolve(MODEL_JAR_FILE_NAME);
    Path jar = createJar(classesDir, fileForJar);
    return new ScoringEngineBuildingStep(jar);
  }

  private Path createJar(Path classesDir, Path fileForJar) throws EnginePublicationException {
    JarOutputStream jar;

    LOGGER.info("Creating model JAR for classes directory: " + classesDir);
    try {
      jar = new JarOutputStream(new FileOutputStream(fileForJar.toString()));

      Files.walk(classesDir).forEach(new Consumer<Path>() {
        @Override
        public void accept(Path classFile) {
          try {
            addClassFileToJar(jar, classFile);
          } catch (IOException e) {
            throw new DirectoryTraversingException(e);
          }
        }
      });

      jar.flush();
      jar.close();
    } catch (IOException | DirectoryTraversingException e) {
      LOGGER.error("Error while creating model jar file: ", e);
      throw new EnginePublicationException("Error while creating model jar file ", e);
    }
    return fileForJar;
  }

  private void addClassFileToJar(JarOutputStream jar, Path classFile) throws IOException {
    if (classFile.toString().endsWith(".class")) {
      jar.putNextEntry(new JarEntry(classFile.getFileName().toString()));
      jar.write(Files.readAllBytes(classFile));
      jar.closeEntry();
    }
  }

  private class DirectoryTraversingException extends RuntimeException {

    private static final long serialVersionUID = 9117429708749767380L;

    public DirectoryTraversingException(Throwable cause) {
      super(cause);
    }
  }

}
