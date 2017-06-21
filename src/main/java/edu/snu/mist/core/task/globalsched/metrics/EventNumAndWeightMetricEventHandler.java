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

import edu.snu.mist.core.task.ExecutionDag;
import edu.snu.mist.core.task.ExecutionVertex;
import edu.snu.mist.core.task.MistPubSubEventHandler;
import edu.snu.mist.core.task.OperatorChain;
import edu.snu.mist.core.task.globalsched.GlobalSchedGroupInfo;
import edu.snu.mist.core.task.globalsched.GlobalSchedGroupInfoMap;
import edu.snu.mist.core.task.metrics.EWMAMetric;
import edu.snu.mist.core.task.metrics.GlobalMetrics;
import edu.snu.mist.core.task.metrics.MetricTrackEvent;
import edu.snu.mist.core.task.metrics.MetricTrackEventHandler;

import javax.inject.Inject;
import java.util.Collection;
import java.util.logging.Logger;

/**
 * A class handles the metric event about weight and the number of events.
 */
public final class EventNumAndWeightMetricEventHandler implements MetricTrackEventHandler {
  private static final Logger LOG = Logger.getLogger(EventNumAndWeightMetricEventHandler.class.getName());

  /**
   * The map of group ids and group info to update.
   */
  private final GlobalSchedGroupInfoMap groupInfoMap;

  /**
   * The global metric holder.
   */
  private final GlobalMetrics globalMetricHolder;

  @Inject
  private EventNumAndWeightMetricEventHandler(final GlobalSchedGroupInfoMap groupInfoMap,
                                              final GlobalMetrics globalMetricHolder,
                                              final MistPubSubEventHandler pubSubEventHandler) {
    this.groupInfoMap = groupInfoMap;
    this.globalMetricHolder = globalMetricHolder;
    pubSubEventHandler.getPubSubEventHandler().subscribe(MetricTrackEvent.class, this);
  }

  @Override
  public void onNext(final MetricTrackEvent metricTrackEvent) {
    long totalNumEvent = 0;
    double totalWeight = 0;
    final Collection<GlobalSchedGroupInfo> groupInfos = groupInfoMap.values();
    for (final GlobalSchedGroupInfo groupInfo : groupInfos) {
      // Track the number of event per each group
      long groupNumEvent = 0;
      for (final ExecutionDag executionDag : groupInfo.getExecutionDags().values()) {
        final Collection<ExecutionVertex> vertices = executionDag.getDag().getVertices();
        for (final ExecutionVertex ev : vertices) {
          if (ev.getType() == ExecutionVertex.Type.OPERATOR_CHAIN) {
            groupNumEvent += ((OperatorChain) ev).numberOfEvents();
          }
        }
      }
      final EWMAMetric numEventMetric = groupInfo.getMetricHolder().getNumEventsMetric();
      numEventMetric.updateValue(groupNumEvent);
      totalNumEvent += groupNumEvent;

      // Set the weight per group
      final double weight = numEventMetric.getEwmaValue();
      groupInfo.getMetricHolder().getWeightMetric().setValue(weight);
      totalWeight += weight;
    }
    globalMetricHolder.getNumEventsMetric().updateValue(totalNumEvent);
    globalMetricHolder.getWeightMetric().setValue(totalWeight);
  }
}