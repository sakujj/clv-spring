package ru.clevertec.house.cache.aop;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import ru.clevertec.house.cache.Cache;
import ru.clevertec.house.entity.IdentifiableByUUID;

import java.util.Optional;
import java.util.UUID;

@Aspect
@Component
@RequiredArgsConstructor
public class CacheAspect {

    private final Cache cache;

    @Around("@annotation(CacheableFindByUUID)")
    public Object findByUUID(ProceedingJoinPoint joinPoint) throws Throwable {
        UUID uuid = getFirstUUIDFromArgs(joinPoint.getArgs());
        synchronized (Cache.class) {
            Optional<IdentifiableByUUID> optional = cache.getById(uuid);
            if (optional.isPresent()) {
                return optional;
            }

            Optional<IdentifiableByUUID> optionalResult = ((Optional<IdentifiableByUUID>) joinPoint.proceed());
            optionalResult.ifPresent(cache::addOrUpdate);

            return optionalResult;
        }

    }

    @Around("@annotation(CacheableDeleteByUUID)")
    public Object deleteByUUID(ProceedingJoinPoint joinPoint) throws Throwable {
        UUID uuid = getFirstUUIDFromArgs(joinPoint.getArgs());
        synchronized (Cache.class) {
            Optional<IdentifiableByUUID> optional = cache.getById(uuid);
            if (optional.isPresent()) {
                cache.removeById(uuid);
            }
            return joinPoint.proceed();
        }
    }

    @Around("@annotation(CacheableUpdateByUUID)")
    public Object updateByUUID(ProceedingJoinPoint joinPoint) throws Throwable {
        synchronized (Cache.class) {
            Optional<IdentifiableByUUID> optionalResult = ((Optional<IdentifiableByUUID>) joinPoint.proceed());
            optionalResult.ifPresent(cache::addOrUpdate);

            return optionalResult;
        }
    }

    @Around("@annotation(CacheableCreate)")
    public Object create(ProceedingJoinPoint joinPoint) throws Throwable {
        synchronized (Cache.class) {
            IdentifiableByUUID result = (IdentifiableByUUID) joinPoint.proceed();
            cache.addOrUpdate(result);

            return result;
        }
    }

    private static UUID getFirstUUIDFromArgs(Object[] args) {
        UUID uuid = null;
        for (var arg : args) {
            if (arg instanceof UUID id) {
                uuid = id;
                break;
            }
        }
        return uuid;
    }
}
