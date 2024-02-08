package io.github.sakujj.cache.aop;

import io.github.sakujj.cache.Cache;
import io.github.sakujj.cache.IdentifiableByUUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Aspect
@RequiredArgsConstructor
public class CacheAspect {

    private final Cache cache;

    @Around("@annotation(CacheableFindByUUID) && args(uuid)")
    public Object findByUUID(ProceedingJoinPoint pjp, UUID uuid) throws Throwable {
        synchronized (Cache.class) {
            log.info("CacheAspect find method was invoked on uuid " + uuid);

            Optional<IdentifiableByUUID> optional = cache.getById(uuid);
            if (optional.isPresent()) {
                return optional;
            }

            Object pjpResult = pjp.proceed();

            if (!(pjpResult instanceof Optional<?> optionalResult)) {

                throw new RuntimeException("An incorrect usage of @CacheableFindByUUID " +
                        ": update method should return Optional<T>, where T is io.github.sakujj.cache.IdentifiableByUUID");
            }

            if (optionalResult.isEmpty()) {
                return optionalResult;
            }

            Object result = optionalResult.get();

            if (!(result instanceof IdentifiableByUUID identifiable)) {

                throw new RuntimeException("An incorrect usage of @CacheableFindByUUID " +
                        ": cached object should be an instance of io.github.sakujj.cache.IdentifiableByUUID");
            }

            cache.addOrUpdate(identifiable);

            return optionalResult;
        }

    }

    @Around("@annotation(CacheableDeleteByUUID) && args(uuid)")
    public Object deleteByUUID(ProceedingJoinPoint pjp, UUID uuid) throws Throwable {
        synchronized (Cache.class) {
            log.info("CacheAspect delete method was invoked on uuid " + uuid);

            Optional<IdentifiableByUUID> optional = cache.getById(uuid);
            if (optional.isPresent()) {
                cache.removeById(uuid);
            }

            return pjp.proceed();
        }
    }

    @Around("@annotation(CacheableUpdateByUUID)")
    public Object updateByUUID(ProceedingJoinPoint pjp) throws Throwable {
        synchronized (Cache.class) {
            log.info("CacheAspect update method was invoked");

            Object pjpResult = pjp.proceed();

            if (!(pjpResult instanceof Optional<?> optionalResult)) {

                throw new RuntimeException("An incorrect usage of @CacheableUpdateByUUID " +
                        ": update method should return Optional<T>, where T is io.github.sakujj.cache.IdentifiableByUUID");
            }

            if (optionalResult.isEmpty()) {
                return optionalResult;
            }

            Object result = optionalResult.get();

            if (!(result instanceof IdentifiableByUUID identifiable)) {

                throw new RuntimeException("An incorrect usage of @CacheableUpdateByUUID " +
                        ": cached object should be an instance of io.github.sakujj.cache.IdentifiableByUUID");
            }

            cache.addOrUpdate(identifiable);

            return optionalResult;
        }
    }

    @Around("@annotation(CacheableCreate)")
    public Object create(ProceedingJoinPoint pjp) throws Throwable {
        synchronized (Cache.class) {
            log.info("CacheAspect create method was invoked");

            Object pjpResult = pjp.proceed();
            if (!(pjpResult instanceof IdentifiableByUUID result)) {

                throw new RuntimeException("An incorrect usage of @CacheableCreate " +
                        ": create method should return an instance of io.github.sakujj.cache.IdentifiableByUUID");
            }

            cache.addOrUpdate(result);

            return result;
        }
    }
}
