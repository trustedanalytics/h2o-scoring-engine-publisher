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
import java.nio.file.Paths;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.trustedanalytics.h2oscoringengine.publisher.EngineBuildingException;
import org.trustedanalytics.h2oscoringengine.publisher.TestCompilationResourcesBuilder;

public class ModelPackagingStepTest {
//TODO: prepare class directory with one broken class file
  private Path compiledClasses;
  private Path expectedJarDir;
  private Path invalidClassesDir = Paths.get("sakfjasj");

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Before
  public void setUp() throws IOException, EngineBuildingException {
    TestCompilationResourcesBuilder resourcesBuilder = new TestCompilationResourcesBuilder();
    this.compiledClasses = resourcesBuilder.prepareCompiledModelClasses();
    this.expectedJarDir = Files.createTempDirectory("h2o-publisher-test-jar");
  }

  @Test
  public void packageModel_outputDirContainsJarFile() throws Exception {
    // given
    ModelPackagingStep step = new ModelPackagingStep(compiledClasses);

    // when
    step.packageModel(expectedJarDir);

    // then
    String[] files = new File(expectedJarDir.toString()).list();
    System.out.println(files);
    assertThat(files, hasItemInArray("model.jar"));
  }

  @Test
  public void packageModel_invalidClassesDir_exceptionThrown() throws Exception {
    // given
    ModelPackagingStep step = new ModelPackagingStep(invalidClassesDir);

    // when
    // then
    thrown.expect(EngineBuildingException.class);
    step.packageModel(expectedJarDir);
    

  }


}
