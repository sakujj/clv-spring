package io.github.sakujj.cache.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Used on methods of a pattern : <i>public Optional&lt;T> *(java.lang.UUID),</p>
 * <p>where T extends {@link io.github.sakujj.cache.IdentifiableByUUID}</i>,</p>
 * <p>to find an object from a cache. If empty, try to find using the method and put in the cache.</p>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CacheableFindByUUID {
}
