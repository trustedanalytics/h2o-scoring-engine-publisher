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
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trustedanalytics.h2oscoringengine.publisher.EngineBuildingException;
import org.trustedanalytics.h2oscoringengine.publisher.EnginePublicationException;

public class ModelCompilationStep {

  private static final Logger LOGGER = LoggerFactory.getLogger(ModelCompilationStep.class);
  private final Path modelPojoPath;
  private final Path libPath;


  public ModelCompilationStep(Path modelPojoPath, Path libPath) {
    this.modelPojoPath = modelPojoPath;
    this.libPath = libPath;
  }

  public ModelPackagingStep compileModel(Path targetDir) throws EngineBuildingException {
    Path classesDir = compile(targetDir);
    return new ModelPackagingStep(classesDir);
  }

  private Path compile(Path targetDir) throws EngineBuildingException {

    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    DiagnosticCollector<JavaFileObject> diagnosticListener =
        new DiagnosticCollector<JavaFileObject>();
    StandardJavaFileManager fileManager =
        compiler.getStandardFileManager(diagnosticListener, null, null);
    Iterable<? extends JavaFileObject> compilationUnit =
        fileManager.getJavaFileObjects(modelPojoPath.toFile());
    List<String> compilerOptions =
        Arrays.asList("-classpath", libPath.toString(), "-d", targetDir.toString());

    LOGGER.info("Compiling file " + modelPojoPath.toString() + " with options " + compilerOptions);

    compiler.getTask(null, fileManager, diagnosticListener, compilerOptions, null, compilationUnit)
        .call();

    if (!diagnosticListener.getDiagnostics().isEmpty()) {
      throw new EngineBuildingException(
          "Model compilation failed: " + compilationFailureMessage(diagnosticListener));
    }

    try {
      fileManager.close();
    } catch (IOException e) {
      LOGGER.warn("Problem while closing files after compilation: ", e);
    }
    return targetDir;
  }

  private String compilationFailureMessage(DiagnosticCollector<JavaFileObject> diagnostics) {
    String message = "";
    for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
      message += diagnostic.getMessage(null);
    }
    return message;
  }
}
