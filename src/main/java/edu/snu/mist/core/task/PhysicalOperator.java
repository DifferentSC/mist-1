/*
 * Copyright (C) 2017 Seoul National University
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
package edu.snu.mist.core.task;

import edu.snu.mist.common.operators.Operator;

/**
 * This interface represents a physical operator that contains the actual object of the operator.
 * It also holds the meta data of the operator.
 */
interface PhysicalOperator extends PhysicalVertex {

  /**
   * Get the actual operator object.
   * @return operator
   */
  Operator getOperator();

  /**
   * Gets the operator chain that contains the operator.
   * @return operator chain that contains the operator.
   */
  OperatorChain getOperatorChain();

  /**
   * Updates the operator chain that contains the operator.
   * @param operatorChain operator chain that contains the operator
   */
  void setOperatorChain(OperatorChain operatorChain);

  /**
   * Get the timestamp of the recently processed data.
   * @return timestamp
   */
  long getLatestDataTimestamp();

  /**
   * Set the timestamp of the recently processed data.
   */
  void setLatestDataTimestamp(long timestamp);

  /**
   * Get the timestamp of the recently processed watermark.
   * @return timestamp
   */
  long getLatestWatermarkTimestamp();

  /**
   * Set the timestamp of the recently processed watermark.
   */
  void setLatestWatermarkTimestamp(long timestamp);
}