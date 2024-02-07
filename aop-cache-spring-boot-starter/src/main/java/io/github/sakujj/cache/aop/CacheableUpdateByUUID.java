package io.github.sakujj.cache.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Used on methods of a pattern : <i>public Optional&lt;T> *(..),</p>
 * <p>where T extends {@link io.github.sakujj.cache.IdentifiableByUUID}</i>,</p>
 * <p>to remove an object from a cache and put an updated version of the object returned from the method.</p>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CacheableUpdateByUUID {
}
