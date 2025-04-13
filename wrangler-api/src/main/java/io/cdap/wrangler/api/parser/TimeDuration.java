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
 * Token implementation for parsing time duration strings like "150ms", "2.5s", etc.
 */
public class TimeDuration implements Token {

  private static final Map<String, Long> UNIT_MULTIPLIERS = new HashMap<>();

  static {
    UNIT_MULTIPLIERS.put("NS", 1L);
    UNIT_MULTIPLIERS.put("MS", 1_000_000L);
    UNIT_MULTIPLIERS.put("S", 1_000_000_000L);
    UNIT_MULTIPLIERS.put("M", 60 * 1_000_000_000L);
    UNIT_MULTIPLIERS.put("H", 3600 * 1_000_000_000L);
  }

  private final String value;
  private final long nanoseconds;

  public TimeDuration(String value) {
    this.value = value.trim();
    String upper = this.value.toUpperCase();
    String numberPart = upper.replaceAll("[^\\d.]", "");
    String unitPart = upper.replaceAll("[\\d.]", "");

    if (!UNIT_MULTIPLIERS.containsKey(unitPart)) {
      throw new IllegalArgumentException("Unknown time unit: " + unitPart);
    }

    double numericValue = Double.parseDouble(numberPart);
    this.nanoseconds = (long) (numericValue * UNIT_MULTIPLIERS.get(unitPart));
  }

  /**
   * @return the canonical duration in nanoseconds
   */
  public long getNanoseconds() {
    return nanoseconds;
  }

  @Override
  public Object value() {
    return nanoseconds;
  }

  @Override
  public TokenType type() {
    return TokenType.TIME_DURATION;
  }

  @Override
  public JsonElement toJson() {
    return new JsonPrimitive(nanoseconds);
  }

  @Override
  public String toString() {
    return value;
  }
}
