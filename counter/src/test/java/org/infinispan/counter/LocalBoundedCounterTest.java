package org.infinispan.counter;

import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.testng.annotations.Test;

/**
 * A simple consistency test for local bounded {@link org.infinispan.counter.api.StrongCounter}.
 *
 * @author Pedro Ruivo
 * @since 9.2
 */
@Test(groups = "functional", testName = "counter.LocalBoundedCounterTest")
public class LocalBoundedCounterTest extends BoundedCounterTest {

   @Override
   protected int clusterSize() {
      return 1;
   }

   @Override
   protected GlobalConfigurationBuilder configure(int nodeId) {
      return new GlobalConfigurationBuilder();
   }
}
