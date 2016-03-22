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
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import org.trustedanalytics.h2oscoringengine.publisher.steps.ModelCompilationStep;

public class TestCompilationResourcesBuilder {

  private final Path tempDir;

  public TestCompilationResourcesBuilder() throws IOException {
    this.tempDir = Files.createTempDirectory("");
  }

  public Path prepareModelJavaFile() throws IOException {
    StringBuffer h2oModelSourceCode = new StringBuffer();
    h2oModelSourceCode.append("import hex.genmodel.GenModel;");
    h2oModelSourceCode.append("public class model extends GenModel {");
    h2oModelSourceCode.append("public model() { super(new String[0],new String[0][0]); }");
    h2oModelSourceCode.append("public hex.ModelCategory getModelCategory() { return hex.ModelCategory.Multinomial; }");
    h2oModelSourceCode.append("public final double[] score0( double[] data, double[] preds ) {");
    h2oModelSourceCode.append("return new double[0];");
    h2oModelSourceCode.append("}");
    h2oModelSourceCode.append("public String getUUID() { return Long.toString(1406937660108778282L); }");
    h2oModelSourceCode.append("}");

    Path sourceFile = tempDir.resolve("model.java");
    return Files.write(sourceFile, h2oModelSourceCode.toString().getBytes());
  }

  public Path prepareLibraryFile() throws IOException {
    String libDependency = "h2o-genmodel-3.0.1.4.jar";
    String libDependencyPath = "/lib/" + libDependency;
    URL resource = this.getClass().getResource(libDependencyPath);
    if (null == resource) {
      throw new IOException("JAR resource " + libDependencyPath + " not found");
    }

    Path libFile = tempDir.resolve(libDependency);
    InputStream lib = this.getClass().getResourceAsStream(libDependencyPath);
    Files.copy(lib, libFile);
    
    return libFile;
  }
  
  public Path prepareModelJavaFileWithCompilationError() throws IOException{
    StringBuffer h2oModelSourceCode = new StringBuffer();
    h2oModelSourceCode.append("import hex.genmodel.GenModel;");
    h2oModelSourceCode.append("public class badmodel extends GenModel {");
    h2oModelSourceCode.append("public model() { super(new String[0],new String[0][0]); }");
    h2oModelSourceCode.append("public hex.ModelCategory getModelCategory() { return hex.ModelCategory.Multinomial; }");
    h2oModelSourceCode.append("public final double[] score0( double[] data, double[] preds ) {");
    h2oModelSourceCode.append("return new double[0];");
    h2oModelSourceCode.append("}");
    h2oModelSourceCode.append("public String getUUID() { return Long.toString(1406937660108778282L); }");

    Path sourceFile = tempDir.resolve("badmodel.java");
    return Files.write(sourceFile, h2oModelSourceCode.toString().getBytes());
  }
  
  public Path prepareCompiledModelClasses() throws EngineBuildingException, IOException{
    ModelCompilationStep compilationStep = new ModelCompilationStep(
        this.prepareModelJavaFile(), this.prepareLibraryFile());
    Path compiledClasses = Files.createTempDirectory("h2o-publisher-test-classes");
    compilationStep.compileModel(compiledClasses);
    
    return compiledClasses;
  }
}
