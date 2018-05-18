/*
 * Copyright (C) 2018 Seoul National University
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
package edu.snu.mist.core.master.lb.scaling;

/**
 * The runner which runs dynamic scaling.
 */
public final class DynamicScalingRunner implements Runnable {

  private final DynamicScalingPolicy dynamicScalingPolicy;

  public DynamicScalingRunner(
      final DynamicScalingPolicy dynamicScalingPolicy
  ) {
    this.dynamicScalingPolicy = dynamicScalingPolicy;
  }

  @Override
  public void run() {
    final ScalingAction scalingAction = dynamicScalingPolicy.getScalingAction();
    if (scalingAction == ScalingAction.SCALE_OUT) {
      // Perform scale-out
    } else if (scalingAction == ScalingAction.SCALE_IN) {
      // Perform scale-in
    }
  }
}