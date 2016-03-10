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


import static org.mockito.Mockito.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.trustedanalytics.h2oscoringengine.publisher.EngineBuildingException;
import org.trustedanalytics.h2oscoringengine.publisher.http.FilesDownloader;

public class H2oResourcesDownloadingStepTest {

  private final String testModelName = "laksjhdf";
  private final String testModelNameWithDash = "sajfkdj-adsjadj-sa";
  private final Path testPath = Paths.get("/askfhj/sadasgd");

  private final String expectedModelFileName = testModelName + ".java";
  private final String expectedModelFileNameForDash = "sajfkdj_adsjadj_sa" + ".java";
  private final Path expectedPathForModel = testPath.resolve(expectedModelFileName);
  private final Path expectedPathForLib =
      testPath.resolve(H2oResourcesDownloadingStep.H2O_LIB_FILE_NAME);
  private final Path expectedPathForModelNameWithDash =
      testPath.resolve(expectedModelFileNameForDash);

  private FilesDownloader downloaderMock = mock(FilesDownloader.class);

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Before
  public void setUp() throws IOException {
    when(downloaderMock.download(
        eq(H2oResourcesDownloadingStep.H2O_SERVER_MODEL_PATH_PREFIX + testModelName),
        eq(expectedPathForModel))).thenReturn(expectedPathForModel);
    when(downloaderMock.download(eq(H2oResourcesDownloadingStep.H2O_SERVER_LIB_PATH),
        eq(expectedPathForLib))).thenReturn(expectedPathForLib);
    when(downloaderMock.download(
        eq(H2oResourcesDownloadingStep.H2O_SERVER_MODEL_PATH_PREFIX + testModelNameWithDash),
        eq(expectedPathForModelNameWithDash))).thenReturn(expectedPathForModelNameWithDash);
  }

  @Test
  public void downloadResources_modelAndLibraryDownloadCallsOccured() throws Exception {
    // given
    H2oResourcesDownloadingStep step = new H2oResourcesDownloadingStep();

    // when
    step.downloadResources(downloaderMock, testModelName, testPath);

    // then
    verify(downloaderMock).download(
        eq(H2oResourcesDownloadingStep.H2O_SERVER_MODEL_PATH_PREFIX + testModelName),
        eq(expectedPathForModel));
    verify(downloaderMock).download(eq(H2oResourcesDownloadingStep.H2O_SERVER_LIB_PATH),
        eq(expectedPathForLib));
  }

  @Test
  public void downloadResources_pathToModelWithProperJavaClassNameReturned() throws Exception {
    // given
    H2oResourcesDownloadingStep step = new H2oResourcesDownloadingStep();

    // when
    ModelCompilationStep result = step.downloadResources(downloaderMock, testModelName, testPath);

    // then
    assertThat(result.getLibPath(), equalTo(expectedPathForLib));
    assertThat(result.getModelPojoPath(), equalTo(expectedPathForModel));
  }

  @Test
  public void downloadResources_modelWithDashInName_pathToModelWithProperJavaClassNameReturned()
      throws Exception {
    // given
    H2oResourcesDownloadingStep step = new H2oResourcesDownloadingStep();

    // when
    ModelCompilationStep result =
        step.downloadResources(downloaderMock, testModelNameWithDash, testPath);

    // then
    assertThat(result.getLibPath(), equalTo(expectedPathForLib));
    assertThat(result.getModelPojoPath(), equalTo(expectedPathForModelNameWithDash));
  }

  @Test
  public void downloadResources_filesDownloaderError_exceptionThrown() throws Exception {
    // given
    H2oResourcesDownloadingStep step = new H2oResourcesDownloadingStep();

    // when
    when(downloaderMock.download(any(), any())).thenThrow(new IOException());

    // then
    thrown.expect(EngineBuildingException.class);
    step.downloadResources(downloaderMock, testModelNameWithDash, testPath);
  }
}
