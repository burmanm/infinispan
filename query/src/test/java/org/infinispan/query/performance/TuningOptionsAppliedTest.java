package org.infinispan.query.performance;

import java.io.IOException;

import org.hibernate.search.backend.configuration.impl.IndexWriterSetting;
import org.hibernate.search.backend.spi.LuceneIndexingParameters;
import org.hibernate.search.backend.spi.LuceneIndexingParameters.ParameterSet;
import org.hibernate.search.indexes.impl.NRTIndexManager;
import org.hibernate.search.indexes.spi.IndexManager;
import org.hibernate.search.spi.SearchIntegrator;
import org.hibernate.search.store.DirectoryProvider;
import org.hibernate.search.store.impl.FSDirectoryProvider;
import org.infinispan.Cache;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.query.Search;
import org.infinispan.query.SearchManager;
import org.infinispan.query.test.Person;
import org.infinispan.test.AbstractInfinispanTest;
import org.infinispan.test.TestingUtil;
import org.infinispan.test.fwk.TestCacheManagerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Verifies the options used for performance tuning are actually being applied to the Search engine
 *
 * @author Sanne Grinovero
 * @since 5.3
 */
@Test(groups = "functional", testName = "query.performance.TuningOptionsAppliedTest")
public class TuningOptionsAppliedTest extends AbstractInfinispanTest {

   public void verifyFSDirectoryOptions() throws IOException {
      EmbeddedCacheManager embeddedCacheManager = TestCacheManagerFactory.fromXml("nrt-performance-writer.xml");
      try {
         SearchIntegrator si = extractSearchFactoryImplementor(embeddedCacheManager);
         NRTIndexManager nrti = verifyShardingOptions(si, 6);
         verifyIndexWriterOptions(nrti, 220, 4096, 30);
         verifyUsesFSDirectory(nrti);
      } finally {
         TestingUtil.killCacheManagers(embeddedCacheManager);
      }
   }

   private SearchIntegrator extractSearchFactoryImplementor(EmbeddedCacheManager embeddedCacheManager) {
      Cache<Object, Object> cache = embeddedCacheManager.getCache("Indexed");
      cache.put("hey this type exists", new Person("id", "name", 3));
      SearchManager searchManager = Search.getSearchManager(cache);
      return searchManager.unwrap(SearchIntegrator.class);
   }

   private NRTIndexManager verifyShardingOptions(SearchIntegrator searchIntegrator, int expectedShards) {
      for (int i = 0; i < expectedShards; i++)
         Assert.assertNotNull(searchIntegrator.getIndexManager("person." + i), "person." + i + " IndexManager missing!");
      Assert.assertNull(searchIntegrator.getIndexManager("person." + expectedShards), "An IndexManager too much was created!");

      IndexManager indexManager = searchIntegrator.getIndexManager("person.0");
      Assert.assertTrue(indexManager instanceof NRTIndexManager);
      NRTIndexManager nrtIM = (NRTIndexManager) indexManager;
      return nrtIM;
   }

   private void verifyUsesFSDirectory(NRTIndexManager nrtIM) {
      DirectoryProvider directoryProvider = nrtIM.getDirectoryProvider();
      Assert.assertTrue(directoryProvider instanceof FSDirectoryProvider);
   }

   private void verifyIndexWriterOptions(NRTIndexManager nrtIM, Integer expectedRAMBuffer, Integer expectedMaxMergeSize, Integer expectedMergeFactor) {
      LuceneIndexingParameters indexingParameters = nrtIM.getIndexingParameters();
      ParameterSet indexParameters = indexingParameters.getIndexParameters();
      Assert.assertEquals(indexParameters.getCurrentValueFor(IndexWriterSetting.RAM_BUFFER_SIZE), expectedRAMBuffer);
      Assert.assertEquals(indexParameters.getCurrentValueFor(IndexWriterSetting.MERGE_MAX_SIZE), expectedMaxMergeSize);
      Assert.assertEquals(indexParameters.getCurrentValueFor(IndexWriterSetting.MERGE_FACTOR), expectedMergeFactor);
   }

}
