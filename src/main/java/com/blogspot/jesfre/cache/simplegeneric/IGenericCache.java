/**
 * 
 */
package com.blogspot.jesfre.cache.simplegeneric;

/**
 * A generic interface for generic enabled cache wrappers. 
 * @author <a href="jorge.ruiz.aquino@gmail.com">Jorge Ruiz Aquino</a>
 * Apr 9, 2016
 * @param <K>
 * @param <V>
 */
public interface IGenericCache<K, V> {

	/**
	 * Puts the new value into the cache.
	 * @param key cannot be null
	 * @param value
	 */
	void put(K key, V value);
	
	/**
	 * Returns the value for the given key.
	 * @param key
	 * @return
	 */
	V get(K key);
	
	/**
	 * Checks if the given key exists in the cache.
	 * @param key
	 * @return
	 */
	boolean containsKey(K key);
	
}
