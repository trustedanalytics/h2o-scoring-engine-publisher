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
package org.trustedanalytics.h2oscoringengine.publisher.http;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class JsonDataFetcher {

  private JsonDataFetcher() {
  }

  public static String getStringValue(String json, String valuePath)
      throws JsonProcessingException, IOException {
    JsonNode root = getJsonRootNode(json);
    return root.at(valuePath).asText();
  }

  public static Integer getIntValue(String json, String valuePath)
      throws JsonProcessingException, IOException {
    JsonNode root = getJsonRootNode(json);
    return root.at(valuePath).asInt();
  }

  private static JsonNode getJsonRootNode(String json) throws JsonProcessingException, IOException {
    ObjectMapper mapper = new ObjectMapper();
    return mapper.readTree(json);
  }
}
