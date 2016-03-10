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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import org.hamcrest.collection.IsArrayWithSize;
import org.junit.Before;
import org.junit.Test;

public class FsDirectoryOperationsTest {

  private Path nonExistingDir;
  private Path existingDir;

  @Before
  public void setUp() throws IOException {
    this.existingDir = Files.createTempDirectory("h2o-publisher-test");
    Files.createTempFile(existingDir, "some-file", "");
    nonExistingDir = existingDir.resolve("some-dir");
  }

  @Test
  public void createEmptyDirectoryTree_directoryNotExists_createsDirectory() throws Exception {
    // given
    FsDirectoryOperations dirOperations = new FsDirectoryOperations();

    // when
    dirOperations.createEmptyDirectoryTree(nonExistingDir);

    // then
    assertThat(Files.exists(nonExistingDir), is(true));
  }

  @Test
  public void createEmptyDirectoryTree_directoryExists_deletesContentOfDirectory()
      throws Exception {
    // given
    FsDirectoryOperations dirOperations = new FsDirectoryOperations();

    // when
    dirOperations.createEmptyDirectoryTree(existingDir);

    // then
    String[] files = new File(existingDir.toString()).list();
    assertThat(files, arrayWithSize(0));
  }

  @Test
  public void createSubdirectory_subdirectoryCreated() throws Exception {
    // given
    FsDirectoryOperations dirOperations = new FsDirectoryOperations();
    String expectedSubDirName = UUID.randomUUID().toString();

    // when
    dirOperations.createSubdirectory(existingDir, expectedSubDirName);

    // then
    assertThat(Files.exists(existingDir.resolve(expectedSubDirName)), is(true));
  }

}
