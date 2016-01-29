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
package edu.snu.mist.task.operators;

import edu.snu.mist.api.types.Tuple2;
import edu.snu.mist.common.parameters.QueryId;
import edu.snu.mist.task.operators.parameters.KeyIndex;
import edu.snu.mist.task.operators.parameters.OperatorId;
import edu.snu.mist.task.ssm.OperatorState;
import edu.snu.mist.task.ssm.SSM;
import org.apache.reef.io.network.util.StringIdentifierFactory;
import org.apache.reef.tang.annotations.Parameter;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * This operator reduces the value by key.
 * @param <K> key type
 * @param <V> value type
 */
public final class ReduceByKeyOperator<K, V> extends StatefulOperator<Tuple2, Map<K, V>, Map<K, V>> {

  /**
   * A reduce function.
   */
  private final BiFunction<V, V, V> reduceFunc;

  /**
   * An index of key.
   */
  private final int keyIndex;

  /**
   * @param ssm ssm for read/update the operator state.
   * @param reduceFunc reduce function
   * @param queryId identifier of the query which contains this operator
   * @param operatorId identifier of operator
   * @param keyIndex index of key
   * @param idFactory identifier factory
   */
  @Inject
  private ReduceByKeyOperator(final SSM ssm,
                              final BiFunction<V, V, V> reduceFunc,
                              @Parameter(QueryId.class) final String queryId,
                              @Parameter(OperatorId.class) final String operatorId,
                              @Parameter(KeyIndex.class) final int keyIndex,
                              final StringIdentifierFactory idFactory) {
    super(ssm, idFactory.getNewInstance(queryId), idFactory.getNewInstance(operatorId));
    this.reduceFunc = reduceFunc;
    this.keyIndex = keyIndex;
  }

  @Override
  public OperatorState<Map<K, V>> getInitialState() {
    return new OperatorState<>(new HashMap<>());
  }

  /**
   * Reduces the value by key.
   * This does not create a new state and
   * does mutable computation for efficient memory use.
   * @param input input tuple
   * @param state previous state
   * @return output
   */
  @SuppressWarnings("unchecked")
  @Override
  public Map<K, V> updateState(final Tuple2 input, final Map<K, V> state) {
    final K key = (K)input.get(keyIndex);
    final V val = (V)input.get(1 - keyIndex);
    final V oldVal = state.get(key);
    if (oldVal == null) {
      state.put(key, val);
    } else {
      state.put(key, reduceFunc.apply(oldVal, val));
    }
    return state;
  }

  /**
   * The state and the generated output are the same, so just emits the state.
   * @param finalState state
   * @return output
   */
  @Override
  public Map<K, V> generateOutput(final Map<K, V> finalState) {
    return finalState;
  }

  @Override
  public String getOperatorClassName() {
    return "ReduceByKeyOperator";
  }
}
