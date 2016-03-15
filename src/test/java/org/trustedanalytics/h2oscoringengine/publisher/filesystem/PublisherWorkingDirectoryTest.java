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

import static org.mockito.Mockito.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Before;
import org.junit.Test;

public class PublisherWorkingDirectoryTest {

  private static String TEST_DIR_NAME = "kajshfskjhf";
  private DirectoryOperations dirOperationsMock;
  private Path expectedMainDir;
  private String expectedSubDirForClasses;
  private String expectedSubDirForH2oResources;
  private Path expectedDirForClasses;
  private Path expectedDirForH2oResources;

  @Before
  public void setUp() throws IOException {
    // given
    this.expectedMainDir = Paths.get(PublisherWorkingDirectory.SYSTEM_TMP,
        PublisherWorkingDirectory.PUBLISHER_PARENT_DIRECTORY_NAME, TEST_DIR_NAME);
    this.expectedSubDirForClasses = PublisherWorkingDirectory.COMPILED_MODEL_SUBDIR_NAME;
    this.expectedSubDirForH2oResources = PublisherWorkingDirectory.H2O_RESOURCES_SUBDIR_NAME;
    this.expectedDirForClasses = expectedMainDir.resolve(expectedSubDirForClasses);
    this.expectedDirForH2oResources = expectedMainDir.resolve(expectedSubDirForH2oResources);

    this.dirOperationsMock = mock(FsDirectoryOperations.class);
    when(dirOperationsMock.createSubdirectory(expectedMainDir, expectedSubDirForClasses))
        .thenReturn(expectedMainDir.resolve(expectedSubDirForClasses));
    when(dirOperationsMock.createSubdirectory(expectedMainDir, expectedSubDirForH2oResources))
        .thenReturn(expectedMainDir.resolve(expectedDirForH2oResources));
  }

  @Test
  public void publisherWorkingDirectory_createsMainDir() throws Exception {
    // when
    new PublisherWorkingDirectory(TEST_DIR_NAME, dirOperationsMock);

    // then
    verify(dirOperationsMock).createEmptyDirectoryTree(expectedMainDir);
  }

  @Test
  public void publisherWorkingDirectory_createsDirForClasses() throws IOException {
    // when
    new PublisherWorkingDirectory(TEST_DIR_NAME, dirOperationsMock);

    // then
    verify(dirOperationsMock).createSubdirectory(expectedMainDir, expectedSubDirForClasses);
  }

  @Test
  public void publisherWorkingDirectory_createsDirForH2oResources() throws IOException {
    // when
    new PublisherWorkingDirectory(TEST_DIR_NAME, dirOperationsMock);

    // then
    verify(dirOperationsMock).createSubdirectory(expectedMainDir, expectedSubDirForH2oResources);
  }

  @Test
  public void getModelJarPath_returnsMainDirAsDirForJar() throws IOException {
    // when
    PublisherWorkingDirectory workingDirectory =
        new PublisherWorkingDirectory(TEST_DIR_NAME, dirOperationsMock);
    Path modelJarPath = workingDirectory.getModelJarPath();

    // then
    assertThat(modelJarPath, is(equalTo(expectedMainDir)));
  }

  @Test
  public void getScoringEngineJarDir_returnsMainDirAsDirForScoringEngine() throws IOException {
    // when
    PublisherWorkingDirectory workingDirectory =
        new PublisherWorkingDirectory(TEST_DIR_NAME, dirOperationsMock);
    Path engineJarDir = workingDirectory.getScoringEngineJarDir();

    // then
    assertThat(engineJarDir, is(equalTo(expectedMainDir)));
  }

  @Test
  public void getCompiledModelPath_returnsExpectedCompiledModelPath() throws IOException {
    // when
    PublisherWorkingDirectory workingDirectory =
        new PublisherWorkingDirectory(TEST_DIR_NAME, dirOperationsMock);
    Path compiledModelPath = workingDirectory.getCompiledModelPath();

    // then
    assertThat(compiledModelPath, is(equalTo(expectedDirForClasses)));
  }

  @Test
  public void getH2oResourcesPath_returnsExpectedH2oResourcesPath() throws IOException {
    // when
    PublisherWorkingDirectory workingDirectory =
        new PublisherWorkingDirectory(TEST_DIR_NAME, dirOperationsMock);
    Path h2oResourcesPath = workingDirectory.getH2oResourcesPath();
    
    //then
    assertThat(h2oResourcesPath, is(equalTo(expectedDirForH2oResources)));
  }

}
