package org.infinispan.persistence.factory;

import static org.infinispan.util.logging.Log.PERSISTENCE;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.infinispan.configuration.cache.StoreConfiguration;
import org.infinispan.factories.scopes.Scope;
import org.infinispan.factories.scopes.Scopes;
import org.infinispan.util.logging.Log;
import org.infinispan.util.logging.LogFactory;

/**
 * Registry for multiple {@link CacheStoreFactory} objects.
 *
 * @author Sebastian Laskawiec
 * @since 7.2
 */
@Scope(Scopes.GLOBAL)
public class CacheStoreFactoryRegistry {

   private static final Log log = LogFactory.getLog(CacheStoreFactoryRegistry.class);

   private List<CacheStoreFactory> factories = new CopyOnWriteArrayList<>();

   public CacheStoreFactoryRegistry() {
      factories.add(new LocalClassLoaderCacheStoreFactory());
   }

   /**
    * Creates new Object based on configuration.
    *
    * @param storeConfiguration Cache store configuration.
    * @return Instance created based on the configuration.
    * @throws org.infinispan.commons.CacheConfigurationException when the instance couldn't be created.
    */
   public Object createInstance(StoreConfiguration storeConfiguration) {
      for(CacheStoreFactory factory : factories) {
         Object instance = factory.createInstance(storeConfiguration);
         if(instance != null) {
            return instance;
         }
      }
      throw PERSISTENCE.unableToInstantiateClass(storeConfiguration.getClass());
   }

   public StoreConfiguration processStoreConfiguration(StoreConfiguration storeConfiguration) {
      for(CacheStoreFactory factory : factories) {
         StoreConfiguration processedConfiguration = factory.processConfiguration(storeConfiguration);
         if(processedConfiguration != null) {
            return processedConfiguration;
         }
      }
      return storeConfiguration;
   }

   /**
    * Adds a new factory for processing.
    *
    * @param cacheStoreFactory Factory to be added.
    */
   public void addCacheStoreFactory(CacheStoreFactory cacheStoreFactory) {
      if(cacheStoreFactory == null) {
         throw PERSISTENCE.unableToAddNullCustomStore();
      }
      factories.add(0, cacheStoreFactory);
   }

   /**
    * Removes all factories associated to this registry.
    */
   public void clearFactories() {
      factories.clear();
   }
}
