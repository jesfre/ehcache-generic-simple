/**
 * 
 */
package com.blogspot.jesfre.cache.simplegeneric;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;

/**
 * A simplistic reduced delegation from {@link Cache}
 * @author <a href="jorge.ruiz.aquino@gmail.com">Jorge Ruiz Aquino</a>
 * Apr 9, 2016
 * @param <K>
 * @param <V>
 */
public class SimpleCache<K,V> implements IGenericCache<K, V> {
	private String cacheName = null;
	private CacheManager cacheManager = null;
	
	/**
	 * Gets the default configuration.
	 * @return
	 */
	public static CacheConfiguration getDefaultConfiguration() {
		return CacheManager.getInstance().getConfiguration().getDefaultCacheConfiguration();
	}
	
	/**
	 * Creates and registers the new cache.
	 * @param cacheName
	 * @param cacheManagerInstance
	 */
	public SimpleCache(String cacheName, CacheManager cacheManagerInstance) {
		this.cacheName = cacheName;
		this.cacheManager = cacheManagerInstance;
		this.register();
	}
	
	/**
	 * Registers this cache to the cache manager using the default configuration. 
	 */
	public void register() {
		cacheManager.addCache(cacheName);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void put(K key, V value) {
		if(getCache() == null) {
			// In case someone had removed the cache
			register();
		}
		Element element = new Element(key, value);
		getCache().put(element);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public V get(K key) {
		Element element = this.getCache().get(key);
		if(element != null) {
			return (V)element.getObjectValue();
		}
		return null;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean containsKey(K key) {
		return getCache().isKeyInCache(key);
	}
	
	/**
	 * Returns the cache wrapped by this instance.
	 * @return
	 */
	public Cache getCache() {
		return this.cacheManager.getCache(cacheName);
	}

	/**
	 * Removes all elements in the cache.
	 */
	public void removeAll() {
		getCache().removeAll();
	}
}
