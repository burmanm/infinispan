package org.jboss.as.clustering.infinispan.subsystem;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.infinispan.AdvancedCache;
import org.infinispan.Cache;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.global.GlobalConfiguration;
import org.infinispan.counter.EmbeddedCounterManagerFactory;
import org.infinispan.counter.api.CounterManager;
import org.infinispan.factories.ComponentRegistry;
import org.infinispan.factories.GlobalComponentRegistry;
import org.infinispan.interceptors.AsyncInterceptor;
import org.infinispan.jmx.JmxStatisticsExposer;
import org.infinispan.lifecycle.ComponentStatus;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.query.SearchManager;
import org.infinispan.remoting.rpc.RpcManager;
import org.infinispan.remoting.transport.Address;
import org.infinispan.security.Security;
import org.infinispan.security.actions.GetCacheComponentRegistryAction;
import org.infinispan.security.actions.GetCacheInterceptorChainAction;
import org.infinispan.security.actions.GetCacheLockManagerAction;
import org.infinispan.security.actions.GetCacheManagerAddress;
import org.infinispan.security.actions.GetCacheManagerClusterAvailabilityAction;
import org.infinispan.security.actions.GetCacheManagerClusterNameAction;
import org.infinispan.security.actions.GetCacheManagerConfigurationAction;
import org.infinispan.security.actions.GetCacheManagerCoordinatorAddress;
import org.infinispan.security.actions.GetCacheManagerIsCoordinatorAction;
import org.infinispan.security.actions.GetCacheManagerStatusAction;
import org.infinispan.security.actions.GetCacheRpcManagerAction;
import org.infinispan.security.actions.GetCacheStatusAction;
import org.infinispan.security.actions.GetGlobalComponentRegistryAction;
import org.infinispan.server.infinispan.actions.ClearCacheAction;
import org.infinispan.server.infinispan.actions.FlushCacheAction;
import org.infinispan.server.infinispan.actions.GetCacheVersionAction;
import org.infinispan.server.infinispan.actions.GetCreatedCacheCountAction;
import org.infinispan.server.infinispan.actions.GetDefinedCacheCountAction;
import org.infinispan.server.infinispan.actions.GetDefinedCacheNamesAction;
import org.infinispan.server.infinispan.actions.GetMembersAction;
import org.infinispan.server.infinispan.actions.GetRunningCacheCountAction;
import org.infinispan.server.infinispan.actions.GetSearchManagerAction;
import org.infinispan.server.infinispan.actions.GetSitesViewAction;
import org.infinispan.server.infinispan.actions.ResetComponentJmxStatisticsAction;
import org.infinispan.server.infinispan.actions.ResetInterceptorJmxStatisticsAction;
import org.infinispan.server.infinispan.actions.StartCacheAction;
import org.infinispan.server.infinispan.actions.StopCacheAction;
import org.infinispan.util.concurrent.locks.LockManager;
import org.jboss.as.clustering.infinispan.DefaultCacheContainer;

/**
 * SecurityActions for the org.jboss.as.clustering.infinispan.subsystem package.
 * <p>
 * Do not move. Do not change class and method visibility to avoid being called from other
 * {@link java.security.CodeSource}s, thus granting privilege escalation to external code.
 *
 * @author Dan Berindei
 * @since 10.0
 */
final class SecurityActions {

   private SecurityActions() {
   }

   private static <T> T doPrivileged(PrivilegedAction<T> action) {
      return System.getSecurityManager() != null ?
            AccessController.doPrivileged(action) : Security.doPrivileged(action);
   }

   private static <T> T doPrivileged(PrivilegedExceptionAction<T> action) throws PrivilegedActionException {
      if (System.getSecurityManager() != null) {
         return AccessController.doPrivileged(action);
      } else {
         return Security.doPrivileged(action);
      }
   }

   static ComponentRegistry getComponentRegistry(final AdvancedCache<?, ?> cache) {
      GetCacheComponentRegistryAction action = new GetCacheComponentRegistryAction(cache);
      return doPrivileged(action);
   }

   static void registerAndStartContainer(final EmbeddedCacheManager container, final Object listener) {
      PrivilegedAction<Void> action = () -> {
         container.addListener(listener);
         container.start();
         return null;
      };
      doPrivileged(action);
   }

   static boolean stopAndUnregisterContainer(final EmbeddedCacheManager container, final Object listener) {
      PrivilegedAction<Boolean> action = () -> {
         if (container.getStatus().allowInvocations()) {
            container.removeListener(listener);
            container.stop();
            return true;
         } else {
            return false;
         }
      };
      return doPrivileged(action);
   }

   static void defineContainerConfiguration(final EmbeddedCacheManager container, final String name,
                                            final Configuration config) {
      PrivilegedAction<Void> action = () -> {
         container.defineConfiguration(name, config);
         return null;
      };
      doPrivileged(action);
   }

   static void undefineContainerConfiguration(final EmbeddedCacheManager container, final String name) {
      PrivilegedAction<Void> action = () -> {
         container.undefineConfiguration(name);
         return null;
      };
      doPrivileged(action);
   }

   static <K, V> Cache<K, V> startCache(final EmbeddedCacheManager container, final String name, final String configurationName) {
      PrivilegedAction<Cache<K, V>> action = () -> {
         Cache<K, V> cache = container.getCache(name, configurationName);
         cache.start();
         return cache;
      };
      return doPrivileged(action);
   }

   static void stopCache(final Cache<?, ?> cache) {
      PrivilegedAction<Void> action = () -> {
         cache.stop();
         return null;
      };
      doPrivileged(action);
   }

   static void shutdownCache(final Cache<?, ?> cache) {
      PrivilegedAction<Void> action = () -> {
         cache.shutdown();
         return null;
      };
      doPrivileged(action);
   }

   static LockManager getLockManager(final AdvancedCache<?, ?> cache) {
      GetCacheLockManagerAction action = new GetCacheLockManagerAction(cache);
      return doPrivileged(action);
   }

   static List<AsyncInterceptor> getInterceptorChain(final AdvancedCache<?, ?> cache) {
      GetCacheInterceptorChainAction action = new GetCacheInterceptorChainAction(cache);
      return doPrivileged(action);
   }

   static RpcManager getRpcManager(final AdvancedCache<?, ?> cache) {
      GetCacheRpcManagerAction action = new GetCacheRpcManagerAction(cache);
      return doPrivileged(action);
   }

   static ComponentStatus getCacheStatus(AdvancedCache<?, ?> cache) {
      GetCacheStatusAction action = new GetCacheStatusAction(cache);
      return doPrivileged(action);
   }

   static String getCacheVersion(AdvancedCache<?, ?> cache) {
      GetCacheVersionAction action = new GetCacheVersionAction(cache);
      return doPrivileged(action);
   }

   static ComponentStatus getCacheManagerStatus(EmbeddedCacheManager cacheManager) {
      GetCacheManagerStatusAction action = new GetCacheManagerStatusAction(cacheManager);
      return doPrivileged(action);
   }

   static Address getCacheManagerLocalAddress(DefaultCacheContainer cacheManager) {
      GetCacheManagerAddress action = new GetCacheManagerAddress(cacheManager);
      return doPrivileged(action);
   }

   static Address getCacheManagerCoordinatorAddress(DefaultCacheContainer cacheManager) {
      GetCacheManagerCoordinatorAddress action = new GetCacheManagerCoordinatorAddress(cacheManager);
      return doPrivileged(action);
   }

   static boolean getCacheManagerIsCoordinator(DefaultCacheContainer cacheManager) {
      GetCacheManagerIsCoordinatorAction action = new GetCacheManagerIsCoordinatorAction(cacheManager);
      return doPrivileged(action);
   }

   static String getCacheManagerClusterName(DefaultCacheContainer cacheManager) {
      GetCacheManagerClusterNameAction action = new GetCacheManagerClusterNameAction(cacheManager);
      return doPrivileged(action);
   }

   static String getCacheManagerClusterAvailability(DefaultCacheContainer cacheManager) {
      GetCacheManagerClusterAvailabilityAction action = new GetCacheManagerClusterAvailabilityAction(cacheManager);
      return doPrivileged(action);
   }

   static String getDefinedCacheNames(DefaultCacheContainer cacheManager) {
      GetDefinedCacheNamesAction action = new GetDefinedCacheNamesAction(cacheManager);
      return doPrivileged(action);
   }

   static String getCacheCreatedCount(DefaultCacheContainer cacheManager) {
      GetCreatedCacheCountAction action = new GetCreatedCacheCountAction(cacheManager);
      return doPrivileged(action);
   }

   static String getDefinedCacheCount(DefaultCacheContainer cacheManager) {
      GetDefinedCacheCountAction action = new GetDefinedCacheCountAction(cacheManager);
      return doPrivileged(action);
   }

   static String getRunningCacheCount(DefaultCacheContainer cacheManager) {
      GetRunningCacheCountAction action = new GetRunningCacheCountAction(cacheManager);
      return doPrivileged(action);
   }

   static List<Address> getMembers(DefaultCacheContainer cacheManager) {
      GetMembersAction action = new GetMembersAction(cacheManager);
      return doPrivileged(action);
   }

   static Void clearCache(AdvancedCache<?, ?> cache) {
      ClearCacheAction action = new ClearCacheAction(cache);
      doPrivileged(action);
      return null;
   }

   static Void flushCache(AdvancedCache<?, ?> cache) {
      FlushCacheAction action = new FlushCacheAction(cache);
      doPrivileged(action);
      return null;
   }

   static Void stopCache(AdvancedCache<?, ?> cache) {
      StopCacheAction action = new StopCacheAction(cache);
      doPrivileged(action);
      return null;
   }

   static Void startCache(AdvancedCache<?, ?> cache) {
      StartCacheAction action = new StartCacheAction(cache);
      doPrivileged(action);
      return null;
   }

   static <T extends JmxStatisticsExposer> Void resetStatistics(AdvancedCache<?, ?> cache,
                                                                Class<T> jmxClass) {
      PrivilegedAction<Void> action;
      if (jmxClass.isAssignableFrom(AsyncInterceptor.class)) {
         action = new ResetInterceptorJmxStatisticsAction(cache, jmxClass);
      } else {
         action = new ResetComponentJmxStatisticsAction(cache, jmxClass);
      }
      doPrivileged(action);
      return null;
   }

   static SearchManager getSearchManager(AdvancedCache<?, ?> cache) {
      GetSearchManagerAction action = new GetSearchManagerAction(cache);
      return doPrivileged(action);
   }

   static GlobalComponentRegistry getGlobalComponentRegistry(final EmbeddedCacheManager cacheManager) {
      GetGlobalComponentRegistryAction action = new GetGlobalComponentRegistryAction(cacheManager);
      return doPrivileged(action);
   }

   static GlobalConfiguration getCacheManagerConfiguration(EmbeddedCacheManager cacheManager) {
      GetCacheManagerConfigurationAction action = new GetCacheManagerConfigurationAction(cacheManager);
      return doPrivileged(action);
   }

   static Set<String> getSitesView(DefaultCacheContainer cacheManager) {
      GetSitesViewAction action = new GetSitesViewAction(cacheManager);
      return doPrivileged(action);
   }

   static Optional<CounterManager> findCounterManager(EmbeddedCacheManager cacheManager) {
      return Optional.ofNullable(doPrivileged((PrivilegedAction<CounterManager>) () -> EmbeddedCounterManagerFactory
            .asCounterManager(cacheManager)));
   }
}
