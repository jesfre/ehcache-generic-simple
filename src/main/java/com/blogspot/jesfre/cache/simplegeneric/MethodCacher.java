package com.blogspot.jesfre.cache.simplegeneric;

import java.lang.reflect.Method;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import com.blogspot.jesfre.cache.simplegeneric.util.ByteCodeUtils;

/**
 * Simple method cache manager to handle method results.
 * TODO make it configurable 
 * @author <a href="jorge.ruiz.aquino@gmail.com">Jorge Ruiz Aquino</a>
 * Apr 14, 2016
 */
public class MethodCacher {
	private static final String METHOD_CACHE_NAME = "_baseMethodCacheControl_";
	protected static MethodCacher baseMethodCacher = null;
	protected static CacheManager cacheManager = null;
	protected int maxElementsInMemory = 1000;
	protected int timeToLiveSeconds = 60*60*24;
	protected int timeToIdleSeconds = timeToLiveSeconds;

	/**
	 * Creates a new method cacher instance.
	 * @param cacheManager the cacheManager used to cache the method results.
	 */
	private MethodCacher(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
		Cache methodCache = new Cache(METHOD_CACHE_NAME, maxElementsInMemory, false, false, timeToLiveSeconds, timeToIdleSeconds);
		cacheManager.addCache(methodCache);
	}

	/**
	 * Returns the singleton BaseMethodCacher. 
	 * @return
	 */
	public static MethodCacher getInstance() {
		if(baseMethodCacher == null) {
			if(cacheManager == null) {
				cacheManager = CacheManager.newInstance();
			}
			baseMethodCacher = new MethodCacher(cacheManager);
		}
		return baseMethodCacher;
	}

	/**
	 * Registers the <b>calling method</b> as a cacheable method so its results will be cached.
	 * @param resultsCache the generic cache object where to store the results.
	 */
	public <K,V> void registerMethod(IGenericCache<K, V>  resultsCache) {
		Method caller = getCallingMethod();
		registerMethod(caller, resultsCache);
	}

	/**
	 * Registers the given method as a cacheable method so its results will be cached.
	 * @param method the method to be cached.
	 * @param resultsCache the generic cache object where to store the results.
	 */
	public <K,V> void registerMethod(Method method, IGenericCache<K, V> resultsCache) {
		Cache methodCache = getMethodCache();
		if(methodCache.isKeyInCache(method)) {
			throw new RuntimeException("The method " + method.getName() + " is already registered.");
		}
		Element newMethod = new Element(method, resultsCache);
		methodCache.put(newMethod);
	}

	/**
	 * Checks if the <b>calling method</b> is registered as cacheable.
	 * @return
	 */
	public boolean isCachedMethod() {
		Method caller = getCallingMethod();
		return isMethodCached(caller);
	}

	/**
	 * Checks if the given method is registered as cacheable.
	 * @param cachedMethod the method caller to test in cache
	 * @return
	 */
	public boolean isMethodCached(Method cachedMethod) {
		Cache methodCache = getMethodCache();
		return methodCache.isKeyInCache(cachedMethod);
	}

	/**
	 * Caches the given results for the <b>calling method</b> parameterized with the given parameter key.
	 * @param parametersKey object-that-is/wrapper-that-contains the parameters used get the original results from the method 
	 * @param results the results to store
	 */
	public <K,V> void storeResults(K parametersKey, V results) {
		Method caller = getCallingMethod();
		storeResults(caller, parametersKey, results);
	}

	/**
	 * Caches the given results for the cached method parameterized with the given parameter key.
	 * @param cachedMethod the method caller to which one belongs the results
	 * @param parametersKey object-that-is/wrapper-that-contains the parameters used get the original results from the method 
	 * @param results the results to store
	 */
	public <K,V> void storeResults(Method cachedMethod, K parametersKey, V results) {
		// Assert result type compatibility
		if(results != null && cachedMethod.getReturnType().isAssignableFrom(results.getClass()) == false) {
			throw new IllegalArgumentException("Expected result of "+cachedMethod.getReturnType()+" but received "+results.getClass());
		}
		Cache methodCache = getMethodCache();
		if(isMethodCached(cachedMethod)) {
			Element resultsEl = methodCache.get(cachedMethod);
			IGenericCache<K, V> resultsCache = (IGenericCache) resultsEl.getObjectValue();
			resultsCache.put(parametersKey, results);
		} else {
			throw new RuntimeException("Method " + cachedMethod.getName() + " is not registered.");
		}
	}

	/**
	 * Checks if the results for the <b>calling method</b> parameterized with the given parameter key is already cached.
	 * @param parametersKey object-that-is/wrapper-that-contains the parameters used get the original results from the method
	 * @return
	 */
	public <K> boolean isResultsCached(K parametersKey) {
		Method caller = getCallingMethod();
		return isResultsCached(caller, parametersKey);
	}

	/**
	 * Checks if the results for the given method parameterized with the given parameter key is already cached.
	 * @param cachedMethod the method caller to which one belongs the results
	 * @param parametersKey object-that-is/wrapper-that-contains the parameters used get the original results from the method
	 * @return
	 */
	public <K> boolean isResultsCached(Method cachedMethod, K parametersKey) {
		Cache methodCache = getMethodCache();
		if(isMethodCached(cachedMethod)) {
			Element resultsEl = methodCache.get(cachedMethod);
			IGenericCache resultsCache = (IGenericCache)resultsEl.getObjectValue();
			return resultsCache.containsKey(parametersKey);
		} else {
			throw new RuntimeException("Method " + cachedMethod.getName() + " is not registered.");
		}
	}

	/**
	 * Gets the cached results for the given method parameterized with the given parameter key.
	 * @param cachedMethod the method caller to which one belongs the results
	 * @param parametersKey object-that-is/wrapper-that-contains the parameters used get the original results from the method
	 * @return
	 */
	public <K, V> V getResults(K parametersKey) {
		Method caller = getCallingMethod();
		return getResults(caller, parametersKey);
	}

	/**
	 * Gets the cached results for the given method parameterized with the given parameter key.
	 * @param cachedMethod the method caller to which one belongs the results
	 * @param parametersKey object-that-is/wrapper-that-contains the parameters used get the original results from the method 
	 * @return
	 */
	public <K, V> V getResults(Method cachedMethod, K parametersKey) {
		Cache methodCache = getMethodCache();
		if(isMethodCached(cachedMethod)) {
			Element resultsEl = methodCache.get(cachedMethod);
			IGenericCache<K, V> resultsCache = (IGenericCache)resultsEl.getObjectValue();
			V results = resultsCache.get(parametersKey);
			return results;
		} else {
			throw new RuntimeException("Method " + cachedMethod.getName() + " is not registered.");
		}
	}

	/**
	 * Discovers the method that is calling to BaseMethodCacher's methods.
	 */
	protected static Method getCallingMethod() {
		Method method = null;
		try {
			StackTraceElement[] stack = Thread.currentThread().getStackTrace();
			if(stack.length > 3) {
				/* The targeted method caller should be the 4th one
				 * 0: Thread
				 * 1: getCallingMethod(..)
				 * 2: the one calling to getCallingMethod(..), e.g. isMethodCached(..)
				 * 3: the targeted method 
				 */
				StackTraceElement caller = stack[3];
				method = ByteCodeUtils.getMethod(caller);
			}
		} catch (Exception e) {
			throw new RuntimeException("Cannot find the calling method.", e);
		}
		return method;
	}

	/**
	 * Gets the cache for the methods.
	 * @return
	 */
	private Cache getMethodCache() {
		return cacheManager.getCache(METHOD_CACHE_NAME);
	}
	
}
