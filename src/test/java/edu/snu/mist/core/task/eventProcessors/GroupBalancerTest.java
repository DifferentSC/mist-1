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
package edu.snu.mist.core.task.eventProcessors;

import edu.snu.mist.core.task.eventProcessors.loadBalancer.GroupBalancer;
import edu.snu.mist.core.task.eventProcessors.loadBalancer.MinLoadGroupBalancerImpl;
import edu.snu.mist.core.task.eventProcessors.loadBalancer.RoundRobinGroupBalancerImpl;
import edu.snu.mist.core.task.eventProcessors.parameters.GroupBalancerGracePeriod;
import edu.snu.mist.core.task.globalsched.GlobalSchedGroupInfo;
import junit.framework.Assert;
import org.apache.reef.io.Tuple;
import org.apache.reef.tang.Injector;
import org.apache.reef.tang.JavaConfigurationBuilder;
import org.apache.reef.tang.Tang;
import org.apache.reef.tang.exceptions.InjectionException;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class GroupBalancerTest {

  private List<Tuple<EventProcessor, List<GlobalSchedGroupInfo>>> currEpGroups;
  private EventProcessor ep1;
  private EventProcessor ep2;

  @Before
  public void setUp() throws InjectionException {
    currEpGroups = new LinkedList<>();
    ep1 = mock(EventProcessor.class);
    ep2 = mock(EventProcessor.class);

    currEpGroups.add(new Tuple<>(ep1, new LinkedList<>()));
    currEpGroups.add(new Tuple<>(ep2, new LinkedList<>()));

  }

  /**
   * Test whether the round-robin group balancer assigns the groups correctly.
   */
  @Test
  public void roundRobinGroupBalancerTest() throws InjectionException {
    final JavaConfigurationBuilder jcb = Tang.Factory.getTang().newConfigurationBuilder();
    final Injector injector = Tang.Factory.getTang().newInjector(jcb.build());
    final GroupBalancer groupBalancer = injector.getInstance(RoundRobinGroupBalancerImpl.class);

    final GlobalSchedGroupInfo group1 = mock(GlobalSchedGroupInfo.class);
    final GlobalSchedGroupInfo group2 = mock(GlobalSchedGroupInfo.class);

    groupBalancer.initialize(currEpGroups);

    groupBalancer.assignGroup(group1, currEpGroups);

    Assert.assertEquals(Arrays.asList(group1), currEpGroups.get(0).getValue());
    Assert.assertEquals(Arrays.asList(), currEpGroups.get(1).getValue());

    groupBalancer.assignGroup(group2, currEpGroups);
    Assert.assertEquals(Arrays.asList(group2), currEpGroups.get(1).getValue());

  }

  /**
   * Check whether the minimum load balancer assigns groups correctly.
   */
  @Test
  public void minLoadBalancerTest() throws InjectionException, InterruptedException {
    final Injector injector = Tang.Factory.getTang().newInjector();
    final long gracePeriod = injector.getNamedInstance(GroupBalancerGracePeriod.class);
    final MinLoadGroupBalancerImpl groupBalancer = injector.getInstance(MinLoadGroupBalancerImpl.class);

    final GlobalSchedGroupInfo group1 = mock(GlobalSchedGroupInfo.class);
    when(group1.getEWMALoad()).thenReturn(10.0);

    final GlobalSchedGroupInfo group2 = mock(GlobalSchedGroupInfo.class);
    when(group2.getEWMALoad()).thenReturn(20.0);

    groupBalancer.initialize(currEpGroups);

    // ep1: [group1]
    // ep2: []
    groupBalancer.assignGroup(group1, currEpGroups);
    Assert.assertEquals(Arrays.asList(group1), currEpGroups.get(0).getValue());

    // ep1: [group1, group2] (load 30.0) -> because of the cached assignment policy
    // ep2: []
    groupBalancer.assignGroup(group2, currEpGroups);
    Assert.assertEquals(Arrays.asList(group1, group2), currEpGroups.get(0).getValue());

    Thread.sleep(gracePeriod * 2);
    final GlobalSchedGroupInfo group3 = mock(GlobalSchedGroupInfo.class);
    when(group3.getEWMALoad()).thenReturn(40.0);

    // ep1: [group1, group2] (load 30.0)
    // ep2: [group3] (load 40.0)
    groupBalancer.assignGroup(group3, currEpGroups);
    Assert.assertEquals(Arrays.asList(group3), currEpGroups.get(1).getValue());

    Thread.sleep(gracePeriod * 2);
    final GlobalSchedGroupInfo group4 = mock(GlobalSchedGroupInfo.class);
    when(group4.getEWMALoad()).thenReturn(20.0);

    // ep1: [group1, group2, group4] (load 50.0)
    // ep2: [group3] (load 40.0)
    groupBalancer.assignGroup(group4, currEpGroups);
    Assert.assertEquals(Arrays.asList(group1, group2, group4), currEpGroups.get(0).getValue());
  }
}
