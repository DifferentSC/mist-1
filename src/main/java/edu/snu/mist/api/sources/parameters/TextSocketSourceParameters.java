/*
 * Copyright (C) 2016 Seoul National University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.snu.mist.api.sources.parameters;

/**
 * This class contains the list of necessary or optional parameters for TextSocketSourceConfiguration.
 */
public final class TextSocketSourceParameters {

  private TextSocketSourceParameters() {
    // Not called.
  }

  /**
   * The host address of the source socket stream.
   */
  public static final String SOCKET_HOST_ADDRESS = "SocketHostAddress";


  /**
   * The host port of the source socket stream.
   */
  public static final String SOCKET_HOST_PORT = "SocketHostPort";

  /**
   * The timestamp extraction function of the source socket stream.
   * This parameter is optional. If it is not set, system consider this stream as processing-time stream.
   */
  public static final String TIMESTAMP_EXTRACTION_FUNCTION = "Timestamp extraction function";
}