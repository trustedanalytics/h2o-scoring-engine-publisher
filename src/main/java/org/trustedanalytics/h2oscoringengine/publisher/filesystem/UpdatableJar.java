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

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.CRC32;

import com.google.common.io.ByteStreams;

public class UpdatableJar implements Closeable {

  private static final String LIB_DIR = "lib/";


  private final Path jarPath;

  private final JarOutputStream jar;

  public UpdatableJar(String fileName, Path targetDirectory) throws IOException {

    String jarFileName = "";
    if (fileName.endsWith(".jar")) {
      jarFileName = fileName;
    } else {
      fileName = fileName.concat(".jar");
    }
    this.jarPath = Paths.get(targetDirectory.toString(), jarFileName);
    this.jar = new JarOutputStream(new FileOutputStream(jarPath.toFile()));
  }


  public UpdatableJar addUncompressedLibJar(Path libJarPath) throws IOException {
    JarEntry libEntry = createUncompressedJarEntry(libJarPath);

    jar.putNextEntry(libEntry);
    jar.write(Files.readAllBytes(libJarPath));
    jar.closeEntry();
    jar.flush();
    jar.close();

    return this;
  }

  public UpdatableJar addJarContent(JarFile jarFile) throws IOException {

    Enumeration<JarEntry> entries = jarFile.entries();
    while (entries.hasMoreElements()) {
      JarEntry jarEntry = entries.nextElement();
      jar.putNextEntry(jarEntry);

      InputStream inputStream = jarFile.getInputStream(jarEntry);
      jar.write(ByteStreams.toByteArray(inputStream));
      jar.closeEntry();
    }
    jarFile.close();
    jar.flush();

    return this;
  }

  public Path getJarPath() {
    return jarPath;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.io.Closeable#close()
   */
  @Override
  public void close() throws IOException {
    jar.close();
  }

  private JarEntry createUncompressedJarEntry(Path jarPath) throws IOException {
    File jarFile = jarPath.toFile();

    JarEntry uncompressedEntry = new JarEntry(LIB_DIR + jarFile.getName());
    uncompressedEntry.setMethod(JarOutputStream.STORED);
    uncompressedEntry.setSize(jarFile.length());
    uncompressedEntry.setCrc(computeCrc32Checksum(jarFile));

    return uncompressedEntry;
  }

  private long computeCrc32Checksum(File file) throws IOException {
    CRC32 checksum = new CRC32();
    checksum.reset();
    checksum.update(Files.readAllBytes(file.toPath()));

    return checksum.getValue();
  }


}
