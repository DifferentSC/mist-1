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
package edu.snu.mist.api.operators;

import edu.snu.mist.api.*;
import edu.snu.mist.api.exceptions.StreamTypeMismatchException;
import edu.snu.mist.api.sources.BaseSourceStream;
import edu.snu.mist.api.types.Tuple2;
import edu.snu.mist.common.DAG;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * The test class for operator APIs.
 */
public final class InstantOperatorStreamTest {

  private MISTQueryBuilder queryBuilder;
  private BaseSourceStream<String> sourceStream;
  private FilterOperatorStream<String> filteredStream;
  private MapOperatorStream<String, Tuple2<String, Integer>> filteredMappedStream;

  @Before
  public void setUp() {
    queryBuilder = new MISTQueryBuilder();
    sourceStream = queryBuilder.socketTextStream(APITestParameters.LOCAL_TEXT_SOCKET_SOURCE_CONF);
    filteredStream = sourceStream.filter(s -> s.contains("A"));
    filteredMappedStream = filteredStream.map(s -> new Tuple2<>(s, 1));
  }

  @After
  public void tearDown() {
    queryBuilder = null;
  }


  /**
   * Test for basic stateless OperatorStreams.
   */
  @Test
  public void testBasicOperatorStream() {
    Assert.assertEquals(filteredMappedStream.getContinuousType(), StreamType.ContinuousType.OPERATOR);
    Assert.assertEquals(filteredMappedStream.getOperatorType(), StreamType.OperatorType.MAP);
    Assert.assertEquals(filteredMappedStream.getMapFunction().apply("A"), new Tuple2<>("A", 1));

    final MISTQuery query = queryBuilder.build();
    final DAG<AvroVertexSerializable, StreamType.Direction> dag = query.getDAG();
    // Check src -> filiter
    final Map<AvroVertexSerializable, StreamType.Direction> neighbors = dag.getEdges(sourceStream);
    Assert.assertEquals(1, neighbors.size());
    Assert.assertEquals(StreamType.Direction.LEFT, neighbors.get(filteredStream));

    // Check filter -> map
    final Map<AvroVertexSerializable, StreamType.Direction> neighbors2 = dag.getEdges(filteredStream);
    Assert.assertEquals(1, neighbors2.size());
    Assert.assertEquals(StreamType.Direction.LEFT, neighbors2.get(filteredMappedStream));
  }

  /**
   * Test for reduceByKey operator.
   */
  @Test
  public void testReduceByKeyOperatorStream() {
    final ReduceByKeyOperatorStream<Tuple2<String, Integer>, String, Integer> reducedStream
        = filteredMappedStream.reduceByKey(0, String.class, (x, y) -> x + y);
    Assert.assertEquals(reducedStream.getBasicType(), StreamType.BasicType.CONTINUOUS);
    Assert.assertEquals(reducedStream.getContinuousType(), StreamType.ContinuousType.OPERATOR);
    Assert.assertEquals(reducedStream.getOperatorType(), StreamType.OperatorType.REDUCE_BY_KEY);
    Assert.assertEquals(reducedStream.getKeyFieldIndex(), 0);
    Assert.assertEquals(reducedStream.getReduceFunction().apply(1, 2), (Integer)3);
    Assert.assertNotEquals(reducedStream.getReduceFunction().apply(1, 3), (Integer) 3);

    // Check filter -> map -> reduceBy
    final MISTQuery query = queryBuilder.build();
    final DAG<AvroVertexSerializable, StreamType.Direction> dag = query.getDAG();
    final Map<AvroVertexSerializable, StreamType.Direction> neighbors = dag.getEdges(filteredMappedStream);
    Assert.assertEquals(1, neighbors.size());
    Assert.assertEquals(StreamType.Direction.LEFT, neighbors.get(reducedStream));
  }

  /**
   * Test for stateful UDF operator.
   */
  @Test
  public void testApplyStatefulOperatorStream() {
    final ApplyStatefulOperatorStream<Tuple2<String, Integer>, Integer, Integer> statefulOperatorStream
        = filteredMappedStream.applyStateful((e, s) -> {
      if (((String) e.get(0)).startsWith("A")) {
        return s + 1;
      } else {
        return s;
      }
    }, s -> s);

    Assert.assertEquals(statefulOperatorStream.getBasicType(), StreamType.BasicType.CONTINUOUS);
    Assert.assertEquals(statefulOperatorStream.getContinuousType(), StreamType.ContinuousType.OPERATOR);
    Assert.assertEquals(statefulOperatorStream.getOperatorType(), StreamType.OperatorType.APPLY_STATEFUL);

    final BiFunction<Tuple2<String, Integer>, Integer, Integer> stateUpdateFunc =
        statefulOperatorStream.getUpdateStateFunc();
    final Function<Integer, Integer> produceResultFunc =
        statefulOperatorStream.getProduceResultFunc();

    /* Simulate two data inputs on UDF stream */
    final int initialState = 0;
    final Tuple2 firstInput = new Tuple2<>("ABC", 1);
    final Tuple2 secondInput = new Tuple2<>("BAC", 1);
    final int firstState = stateUpdateFunc.apply(firstInput, initialState);
    final int firstResult = produceResultFunc.apply(firstState);
    final int secondState = stateUpdateFunc.apply(secondInput, firstState);
    final int secondResult = produceResultFunc.apply(secondState);

    Assert.assertEquals(1, firstState);
    Assert.assertEquals(1, firstResult);
    Assert.assertEquals(1, secondState);
    Assert.assertEquals(1, secondResult);

    // Check filter -> map -> applyStateful
    final MISTQuery query = queryBuilder.build();
    final DAG<AvroVertexSerializable, StreamType.Direction> dag = query.getDAG();
    final Map<AvroVertexSerializable, StreamType.Direction> neighbors = dag.getEdges(filteredMappedStream);
    Assert.assertEquals(1, neighbors.size());
    Assert.assertEquals(StreamType.Direction.LEFT, neighbors.get(statefulOperatorStream));
  }

  /**
   * Test for union operator.
   */
  @Test
  public void testUnionOperatorStream() throws StreamTypeMismatchException {
    final MapOperatorStream<String, Tuple2<String, Integer>> filteredMappedStream2 = queryBuilder
        .socketTextStream(APITestParameters.LOCAL_TEXT_SOCKET_SOURCE_CONF)
            .filter(s -> s.contains("A"))
            .map(s -> new Tuple2<>(s, 1));

    final UnionOperatorStream<Tuple2<String, Integer>> unifiedStream
        = filteredMappedStream.union(filteredMappedStream2);

    Assert.assertEquals(unifiedStream.getBasicType(), StreamType.BasicType.CONTINUOUS);
    Assert.assertEquals(unifiedStream.getContinuousType(), StreamType.ContinuousType.OPERATOR);
    Assert.assertEquals(unifiedStream.getOperatorType(), StreamType.OperatorType.UNION);

    // Check filteredMappedStream (LEFT)  ---> union
    //       filteredMappedStream2 (RIGHT) --/
    final MISTQuery query = queryBuilder.build();
    final DAG<AvroVertexSerializable, StreamType.Direction> dag = query.getDAG();
    final Map<AvroVertexSerializable, StreamType.Direction> n1 = dag.getEdges(filteredMappedStream);
    final Map<AvroVertexSerializable, StreamType.Direction> n2 = dag.getEdges(filteredMappedStream2);

    Assert.assertEquals(1, n1.size());
    Assert.assertEquals(1, n2.size());
    Assert.assertEquals(StreamType.Direction.LEFT, n1.get(unifiedStream));
    Assert.assertEquals(StreamType.Direction.RIGHT, n2.get(unifiedStream));
  }
}