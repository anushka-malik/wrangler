/*
 *  Copyright © 2017-2019 Cask Data, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

 package io.cdap.directives.aggregates;

 import io.cdap.wrangler.api.Arguments;
 import io.cdap.wrangler.api.Directive;
 import io.cdap.wrangler.api.ExecutorContext;
 import io.cdap.wrangler.api.Row;
 import io.cdap.wrangler.api.parser.TokenType;
 import io.cdap.wrangler.api.parser.UsageDefinition;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * A directive that aggregates byte sizes and time durations across rows,
  * outputting totals in MB and seconds.
  */
 public class AggregateStats implements Directive {
 
   /** Input column for byte size. */
   private String byteSizeCol;
 
   /** Input column for duration (nanoseconds). */
   private String durationCol;
 
   /** Output column for byte size in MB. */
   private String outputByteCol;
 
   /** Output column for time in seconds. */
   private String outputTimeCol;
 
   /** Accumulated total bytes. */
   private long totalBytes = 0;
 
   /** Accumulated total duration in nanoseconds. */
   private long totalNanos = 0;
 
   /**
    * Defines the usage of this directive.
    *
    * @return UsageDefinition object
    */
   @Override
   public UsageDefinition define() {
     UsageDefinition.Builder builder = UsageDefinition.builder("aggregate-stats");
     builder.define("byteCol", TokenType.COLUMN_NAME);
     builder.define("durationCol", TokenType.COLUMN_NAME);
     builder.define("outputByteCol", TokenType.COLUMN_NAME);
     builder.define("outputDurationCol", TokenType.COLUMN_NAME);
     return builder.build();
   }
 
   /**
    * Initializes the directive with arguments.
    *
    * @param arguments the directive arguments
    */
   @Override
   public void initialize(final Arguments arguments) {
     byteSizeCol = arguments.value("byteCol");
     durationCol = arguments.value("durationCol");
     outputByteCol = arguments.value("outputByteCol");
     outputTimeCol = arguments.value("outputDurationCol");
   }
 
   /**
    * No-op destroy method.
    */
   @Override
   public void destroy() {
     // No-op
   }
 
   /**
    * Executes the aggregation logic.
    *
    * @param rows    the input rows
    * @param context the execution context
    * @return a list containing one row with aggregate results
    */
   @Override
   public List<Row> execute(final List<Row> rows, final ExecutorContext context) {
     for (Row row : rows) {
       Object byteVal = row.getValue(byteSizeCol);
       Object timeVal = row.getValue(durationCol);
 
       if (byteVal instanceof Number) {
         totalBytes += ((Number) byteVal).longValue();
       }
 
       if (timeVal instanceof Number) {
         totalNanos += ((Number) timeVal).longValue();
       }
     }
 
     Row output = new Row();
     output.add(outputByteCol, convertBytesToMB(totalBytes));
     output.add(outputTimeCol, convertNanosToSeconds(totalNanos));
 
     List<Row> result = new ArrayList<>();
     result.add(output);
     return result;
   }
 
   /**
    * Converts bytes to megabytes.
    *
    * @param bytes total bytes
    * @return bytes in MB
    */
   private double convertBytesToMB(final long bytes) {
     final double bytesInMB = 1024.0 * 1024.0;
     return bytes / bytesInMB;
   }
 
   /**
    * Converts nanoseconds to seconds.
    *
    * @param nanos total nanoseconds
    * @return time in seconds
    */
   private double convertNanosToSeconds(final long nanos) {
     final double nanosInSec = 1_000_000_000.0;
     return nanos / nanosInSec;
   }
 }
 
 