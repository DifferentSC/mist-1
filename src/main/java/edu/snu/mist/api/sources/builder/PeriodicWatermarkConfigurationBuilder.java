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
package edu.snu.mist.api.sources.builder;

import edu.snu.mist.api.configurations.MISTConfiguration;
import edu.snu.mist.api.configurations.MISTConfigurationBuilderImpl;
import edu.snu.mist.api.sources.parameters.PeriodicWatermarkParameters;

import java.util.Arrays;
import java.util.Map;

/**
 * This class builds periodic WatermarkConfiguration.
 */
public final class PeriodicWatermarkConfigurationBuilder extends MISTConfigurationBuilderImpl {

  /**
   * Required parameters for periodic WatermarkConfiguration.
   */
  private final String[] periodicWatermarkRequiredParameters = {
      PeriodicWatermarkParameters.PERIOD,
      PeriodicWatermarkParameters.EXPECTED_DELAY
  };

  public PeriodicWatermarkConfigurationBuilder() {
    requiredParameters.addAll(Arrays.asList(periodicWatermarkRequiredParameters));
  }

  @Override
  protected <T extends MISTConfiguration> T buildConfigMap(final Map<String, Object> configMap) {
    return (T) new PeriodicWatermarkConfiguration(configMap);
  }
}