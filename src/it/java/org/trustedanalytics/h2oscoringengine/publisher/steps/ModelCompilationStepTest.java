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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItemInArray;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.trustedanalytics.h2oscoringengine.publisher.EngineBuildingException;
import org.trustedanalytics.h2oscoringengine.publisher.TestCompilationResourcesBuilder;

public class ModelCompilationStepTest {

  private Path testModelJavaFile;
  private Path testModelJavaFileWithCompilationError;
  private Path testLibFile;
  private Path expectedClassesDir;
  
  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Before
  public void setUp() throws IOException {
    TestCompilationResourcesBuilder resourcesBuilder = new TestCompilationResourcesBuilder();
    this.testModelJavaFile = resourcesBuilder.prepareModelJavaFile("model");
    this.testLibFile = resourcesBuilder.prepareLibraryFile();
    this.testModelJavaFileWithCompilationError = resourcesBuilder.prepareModelJavaFileWithCompilationError();
    this.expectedClassesDir = Files.createTempDirectory("h2o-publisher-test");
  }

  @Test
  public void compileModel_targetDirContainsClassFile() throws Exception {
    // given
    ModelCompilationStep step = new ModelCompilationStep(testModelJavaFile, testLibFile);

    // when
    step.compileModel(expectedClassesDir);

    // then
    String[] files = new File(expectedClassesDir.toString()).list();
    System.out.println(files);
    assertThat(files, hasItemInArray("model.class"));
  }
  
  @Test
  public void compileModel_modelClassWithCompilationError_exceptionThrown() throws Exception {
    // given
    ModelCompilationStep step = new ModelCompilationStep(testModelJavaFileWithCompilationError, testLibFile);

    // when

    // then
    thrown.expect(EngineBuildingException.class);
    step.compileModel(expectedClassesDir);
    
  }

  // @Test
  public void getModelPojoPath() throws Exception {
    throw new RuntimeException("not yet implemented");
  }

  // @Test
  public void getLibPath() throws Exception {
    throw new RuntimeException("not yet implemented");
  }

}
