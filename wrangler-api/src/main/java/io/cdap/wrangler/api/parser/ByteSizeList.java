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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a list of byte size values parsed from the recipe.
 */
public class ByteSizeList implements Token {
  private final List<ByteSize> byteSizes;

  public ByteSizeList(List<ByteSize> byteSizes) {
    this.byteSizes = byteSizes == null ? Collections.emptyList() : new ArrayList<>(byteSizes);
  }

  public List<ByteSize> getByteSizes() {
    return Collections.unmodifiableList(byteSizes);
  }

  @Override
  public Object value() {
    List<Long> values = new ArrayList<>();
    for (ByteSize bs : byteSizes) {
      values.add(bs.getBytes());
    }
    return values;
  }

  @Override
  public TokenType type() {
    return TokenType.BYTE_SIZE;
  }

  @Override
  public JsonElement toJson() {
    JsonArray array = new JsonArray();
    for (ByteSize bs : byteSizes) {
      array.add(new JsonPrimitive(bs.getBytes())); // ✅ Fix: wrap long in JsonPrimitive
    }
    return array;
  }

  @Override
  public String toString() {
    return byteSizes.toString();
  }
}
