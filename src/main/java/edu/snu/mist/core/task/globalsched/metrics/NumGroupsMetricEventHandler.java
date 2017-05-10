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
package edu.snu.mist.core.task.globalsched.metrics;

import edu.snu.mist.core.task.MistPubSubEventHandler;
import edu.snu.mist.core.task.globalsched.GlobalSchedGroupInfoMap;
import edu.snu.mist.core.task.metrics.MetricTrackEvent;
import edu.snu.mist.core.task.metrics.MetricTrackEventHandler;

import javax.inject.Inject;

/**
 * A class handles the metric event about NumGroupsMetric.
 */
public final class NumGroupsMetricEventHandler implements MetricTrackEventHandler {

  /**
   * The map of group ids and group info to update.
   */
  private final GlobalSchedGroupInfoMap groupInfoMap;

  /**
   * The global metrics.
   */
  private final GlobalSchedGlobalMetrics globalMetrics;

  @Inject
  private NumGroupsMetricEventHandler(final GlobalSchedGroupInfoMap groupInfoMap,
                                      final GlobalSchedGlobalMetrics globalMetrics,
                                      final MistPubSubEventHandler pubSubEventHandler) {
    this.groupInfoMap = groupInfoMap;
    this.globalMetrics = globalMetrics;
    // Initialize
    this.onNext(new MetricTrackEvent());
    pubSubEventHandler.getPubSubEventHandler().subscribe(MetricTrackEvent.class, this);
  }

  @Override
  public void onNext(final MetricTrackEvent metricTrackEvent) {
    final long numGroups = groupInfoMap.size();
    globalMetrics.getNumGroupsMetric().setNumGroups(numGroups);
  }
}