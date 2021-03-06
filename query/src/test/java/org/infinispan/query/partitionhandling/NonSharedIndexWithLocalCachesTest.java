package org.infinispan.query.partitionhandling;

import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.query.test.Person;
import org.testng.annotations.Test;

/**
 * @since 9.3
 */
@Test(groups = "functional", testName = "query.partitionhandling.NonSharedIndexWithLocalCachesTest")
public class NonSharedIndexWithLocalCachesTest extends NonSharedIndexTest {

   @Override
   protected ConfigurationBuilder cacheConfiguration() {
      ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
      configurationBuilder.indexing()
            .enable()
            .addIndexedEntity(Person.class)
            .addProperty("default.indexmanager", "near-real-time")
            .addProperty("default.directory_provider", "local-heap");
      return configurationBuilder;
   }

}
