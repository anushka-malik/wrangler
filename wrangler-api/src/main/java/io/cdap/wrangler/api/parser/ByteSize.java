/*
 * Copyright © 2017-2019 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package io.cdap.wrangler.api.parser;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import java.util.HashMap;
import java.util.Map;

/**
 * Token implementation for parsing byte size strings like "10KB", "1.5MB", etc.
 */
public class ByteSize implements Token {

  private static final Map<String, Long> UNIT_MULTIPLIERS = new HashMap<>();

  static {
    UNIT_MULTIPLIERS.put("B", 1L);
    UNIT_MULTIPLIERS.put("KB", 1024L);
    UNIT_MULTIPLIERS.put("MB", 1024L * 1024);
    UNIT_MULTIPLIERS.put("GB", 1024L * 1024 * 1024);
    UNIT_MULTIPLIERS.put("TB", 1024L * 1024 * 1024 * 1024);
  }

  private final String value;
  private final long bytes;

  public ByteSize(String value) {
    this.value = value.trim();
    String upper = this.value.toUpperCase();
    String numberPart = upper.replaceAll("[^\\d.]", "");
    String unitPart = upper.replaceAll("[\\d.]", "");

    if (!UNIT_MULTIPLIERS.containsKey(unitPart)) {
      throw new IllegalArgumentException("Unknown byte size unit: " + unitPart);
    }

    double numericValue = Double.parseDouble(numberPart);
    this.bytes = (long) (numericValue * UNIT_MULTIPLIERS.get(unitPart));
  }

  /**
   * @return the canonical byte value
   */
  public long getBytes() {
    return bytes;
  }

  @Override
  public Object value() {
    return bytes;
  }

  @Override
  public TokenType type() {
    return TokenType.BYTE_SIZE;
  }

  @Override
  public JsonElement toJson() {
    return new JsonPrimitive(bytes);
  }

  @Override
  public String toString() {
    return value;
  }
}
