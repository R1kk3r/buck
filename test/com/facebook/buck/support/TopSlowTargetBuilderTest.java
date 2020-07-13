/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.facebook.buck.support;

import static org.junit.Assert.assertEquals;

import com.facebook.buck.core.model.BuildTarget;
import com.facebook.buck.core.model.BuildTargetFactory;
import com.google.common.collect.ImmutableList;
import org.junit.Test;

public class TopSlowTargetBuilderTest {
  @Test
  public void testNoRules() {
    TopSlowTargetsBuilder builder = new TopSlowTargetsBuilder();
    ImmutableList<SlowTarget> slowTargets = builder.getSlowRules();
    assertEquals(0, slowTargets.size());
  }

  @Test
  public void testOneRule() {
    TopSlowTargetsBuilder builder = new TopSlowTargetsBuilder();
    BuildTarget a = BuildTargetFactory.newInstance("//foo/bar:baz");
    builder.onTargetCompleted(a, 42);
    ImmutableList<SlowTarget> slowTargets = builder.getSlowRules();
    assertEquals(1, slowTargets.size());
    assertEquals(a, slowTargets.get(0).getTarget());
    assertEquals(42, slowTargets.get(0).getDurationMilliseconds());
  }

  @Test
  public void testReplacingSlowestTarget() {
    TopSlowTargetsBuilder builder = new TopSlowTargetsBuilder(2);
    BuildTarget a = BuildTargetFactory.newInstance("//foo/bar:a");
    BuildTarget b = BuildTargetFactory.newInstance("//foo/bar:b");
    BuildTarget c = BuildTargetFactory.newInstance("//foo/bar:c");
    builder.onTargetCompleted(a, 40);
    builder.onTargetCompleted(b, 30);

    // Build target c is the new slowest rule and resets the slowestTarget head pointer.
    // It also kicks b off the end of the list, since we're only keeping around the top 2 slow
    // rules.
    builder.onTargetCompleted(c, 50);

    ImmutableList<SlowTarget> slowTargets = builder.getSlowRules();
    assertEquals(2, slowTargets.size());
    assertEquals(c, slowTargets.get(0).getTarget());
    assertEquals(50, slowTargets.get(0).getDurationMilliseconds());
    assertEquals(a, slowTargets.get(1).getTarget());
    assertEquals(40, slowTargets.get(1).getDurationMilliseconds());
  }

  @Test
  public void testReplacingTail() {
    TopSlowTargetsBuilder builder = new TopSlowTargetsBuilder(2);
    BuildTarget a = BuildTargetFactory.newInstance("//foo/bar:a");
    BuildTarget b = BuildTargetFactory.newInstance("//foo/bar:b");
    BuildTarget c = BuildTargetFactory.newInstance("//foo/bar:c");
    builder.onTargetCompleted(a, 40);
    builder.onTargetCompleted(b, 30);
    builder.onTargetCompleted(c, 35);

    ImmutableList<SlowTarget> slowTargets = builder.getSlowRules();
    assertEquals(2, slowTargets.size());
    assertEquals(a, slowTargets.get(0).getTarget());
    assertEquals(40, slowTargets.get(0).getDurationMilliseconds());
    assertEquals(c, slowTargets.get(1).getTarget());
    assertEquals(35, slowTargets.get(1).getDurationMilliseconds());
  }
}