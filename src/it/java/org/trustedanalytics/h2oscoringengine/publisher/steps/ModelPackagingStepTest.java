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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.trustedanalytics.h2oscoringengine.publisher.EngineBuildingException;
import org.trustedanalytics.h2oscoringengine.publisher.TestCompilationResourcesBuilder;
import org.trustedanalytics.h2oscoringengine.publisher.steps.ModelPackagingStep.DirectoryTraversingException;

public class ModelPackagingStepTest {

  private Path compiledClasses;
  private Path expectedJarDir;
  private Path invalidClassesDir = Paths.get("sakfjasj");
  private Path classFileMock = mock(Path.class);
  private JarOutputStream jarMock = mock(JarOutputStream.class);
  private String testClassFileName = "something.class";
  private String testFileNameWithoutExtension = "askjajkfljkalg";

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

  @Test
  public void addClassFileToJar_fileWithClassExtension_fileAddedToJar() throws IOException {
    // given
    ModelPackagingStep step = new ModelPackagingStep(compiledClasses);
    when(classFileMock.toString()).thenReturn(testClassFileName);
    when(classFileMock.getFileName()).thenReturn(Paths.get("", testClassFileName));
    ArgumentCaptor<JarEntry> jarEntryCaptor = ArgumentCaptor.forClass(JarEntry.class);

    // when
    step.addClassFileToJar(jarMock, classFileMock);

    // then
    verify(jarMock).putNextEntry(jarEntryCaptor.capture());
  }

  @Test
  public void addClassFileToJar_fileWithoutClassExtension_fileNotAddedToJar() throws IOException {
    // given
    ModelPackagingStep step = new ModelPackagingStep(compiledClasses);
    when(classFileMock.toString()).thenReturn(testFileNameWithoutExtension);
    when(classFileMock.getFileName()).thenReturn(Paths.get("", testFileNameWithoutExtension));
    ArgumentCaptor<JarEntry> jarEntryCaptor = ArgumentCaptor.forClass(JarEntry.class);

    // when
    step.addClassFileToJar(jarMock, classFileMock);

    // then
    verify(jarMock, never()).putNextEntry(jarEntryCaptor.capture());
  }

  @Test
  public void addClassFileToJar_ioExceptionFromJarStream_exceptionThrown() throws IOException {
    // given
    ModelPackagingStep step = new ModelPackagingStep(compiledClasses);
    when(classFileMock.toString()).thenReturn(testClassFileName);
    when(classFileMock.getFileName()).thenReturn(Paths.get("", testClassFileName));
    doThrow(new IOException()).when(jarMock).closeEntry();

    // when
    // then
    thrown.expect(DirectoryTraversingException.class);
    step.addClassFileToJar(jarMock, classFileMock);
  }


}
