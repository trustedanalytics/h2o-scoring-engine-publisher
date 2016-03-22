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

import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.MatcherAssert.assertThat;

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

public class ScoringEngineBuildingStepTest {
  
  private String engineBaseResourcePath = "/runtime/h2o-scoring-engine-base-0.4.10.jar";
  private String invalidResourcePath = "/sajfdk/dkgjfk.jar";
  private Path compiledClasses;
  private Path jarDir;
  private Path expectedScoringEngineDir;
  private Path invalidDirForScoringEngine = Paths.get("slkjfs");

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Before
  public void setUp() throws IOException, EngineBuildingException {
    TestCompilationResourcesBuilder resourcesBuilder = new TestCompilationResourcesBuilder();
    this.compiledClasses = resourcesBuilder.prepareCompiledModelClasses();
    this.jarDir = Files.createTempDirectory("h2o-publisher-test-jar");
    this.expectedScoringEngineDir = Files.createTempDirectory("h2o-publisher-test-engine");
  }

  @Test
  public void buildScoringEngine_returnsJar() throws Exception {
    // given 
    ScoringEngineBuildingStep step = new ModelPackagingStep(compiledClasses).packageModel(jarDir);
    
    // when
    Path scoringEngineJar = step.buildScoringEngine(expectedScoringEngineDir, engineBaseResourcePath);
    
    // then
    assertThat(scoringEngineJar.toString(), endsWith(".jar"));
  }
  
  @Test
  public void buildScoringEngine_invalidDir_exceptionThrown() throws Exception {
    // given 
    ScoringEngineBuildingStep step = new ModelPackagingStep(compiledClasses).packageModel(jarDir);
    
    // when
    // then
    thrown.expect(EngineBuildingException.class);
    step.buildScoringEngine(invalidDirForScoringEngine, engineBaseResourcePath);
    
  }
  
  @Test
  public void buildScoringEngine_resourceNotFound_exceptionThrown() throws EngineBuildingException {
    //given
    ScoringEngineBuildingStep step = new ModelPackagingStep(compiledClasses).packageModel(jarDir);
    
    //when
    //then
    thrown.expect(EngineBuildingException.class);
    step.buildScoringEngine(expectedScoringEngineDir, invalidResourcePath);
    
  }

}
