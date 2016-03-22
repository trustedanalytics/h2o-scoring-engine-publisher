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

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FsDirectoryOperations implements DirectoryOperations {

  private static final Logger LOGGER = LoggerFactory.getLogger(FsDirectoryOperations.class);

  @Override
  public Path createEmptyDirectoryTree(Path path) throws IOException {
    if (Files.exists(path)) {
      LOGGER.info("Directory " + path.toString() + " exists. Deleting all files...");
      clearDirectory(path);
    }
    return Files.createDirectories(path);
  }

  @Override
  public Path createSubdirectory(Path dir, String subdirectoryName) throws IOException {
    Path subDirPath = dir.resolve(subdirectoryName);
    return Files.createDirectory(subDirPath);
  }

  private void clearDirectory(Path dir) throws IOException {
    Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {

      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        LOGGER.debug("Deleting file: " + file.toString());
        Files.delete(file);
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        LOGGER.info("Deleting directory: " + dir.toString());
        Files.delete(dir);
        return FileVisitResult.CONTINUE;
      }

    });
  }
}
