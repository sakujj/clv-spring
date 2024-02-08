package io.github.sakujj.cache.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Used on methods of a pattern : <i>public * *(java.util.UUID),</p>
 * <p>to remove an object from a cache by specified uuid and then call the method.</p>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CacheableDeleteByUUID {
}
