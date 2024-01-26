package ru.clevertec.house.cache;


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import ru.clevertec.house.dto.HouseRequest;
import ru.clevertec.house.dto.HouseResponse;
import ru.clevertec.house.repository.AbstractDatabaseIntegrationTests;
import ru.clevertec.house.service.HouseService;
import ru.clevertec.house.test.util.HouseTestBuilder;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Transactional
public abstract class AbstractCacheTests extends AbstractDatabaseIntegrationTests {

    private static final int THREAD_COUNT = 6;

    @SpyBean
    private Cache cache;

    private ExecutorService executorService;

    @Autowired
    private HouseService houseService;

    @AfterEach
    void clear() {
        cache.clear();
    }

    @Test
    public void cacheShouldStayTheSameSize() throws InterruptedException {
        // given
        executorService = Executors.newFixedThreadPool(THREAD_COUNT);

        HouseResponse houseResponse = houseService.findAll(PageRequest.of(0, 1))
                .getContent()
                .get(0);
        UUID uuid = houseResponse.getUuid();

        CountDownLatch latch = new CountDownLatch(40);

        IntStream.range(0, 10).forEach(i -> executorService.submit(() ->
        {
            houseService.findByUUID(uuid);
            latch.countDown();
        }));

        IntStream.range(0, 20).forEach(i -> executorService.submit(() ->
        {
            HouseRequest requestFirst = HouseTestBuilder.aHouse()
                    .withNumber(i + 100)
                    .buildRequest();
            houseService.update(requestFirst, uuid);
            latch.countDown();
        }));


        IntStream.range(0, 10).forEach(i -> executorService.submit(() ->
        {
            houseService.findByUUID(uuid);
            latch.countDown();
        }));

        latch.await();
        executorService.shutdown();

        // when
        int actual = cache.getSize();

        // then
        assertThat(actual).isEqualTo(1);

        verify(cache, atLeast(20)).getById(uuid);
        // 1 time in findByUUID advice and 20 times in updateByUUID advice
        verify(cache, times(21)).addOrUpdate(any());
    }


    @Test
    public void cacheShouldRemoveElementAfterDelete() {
        // given
        UUID uuid = UUID.fromString("e89895ef-ca4c-433b-87e8-3ead2646fed1");

        // when
        houseService.findByUUID(uuid);
        int cacheSizeAfterFirstFind = cache.getSize();

        houseService.deleteByUUID(uuid);
        int cacheSizeAfterDelete = cache.getSize();

        houseService.findByUUID(uuid);
        int cacheSizeAfterSecondFind = cache.getSize();

        // then
        assertThat(cacheSizeAfterFirstFind).isEqualTo(1);
        assertThat(cacheSizeAfterDelete).isEqualTo(0);
        assertThat(cacheSizeAfterSecondFind).isEqualTo(0);

        verify(cache, times(3)).getById(uuid);
        verify(cache).removeById(uuid);
    }

    @Test
    public void cacheShouldContainElementAfterCreate() {
        // given
        HouseRequest houseRequest = HouseTestBuilder.aHouse().buildRequest();

        // when
        HouseResponse created = houseService.create(houseRequest);
        int cacheSizeAfterCreate = cache.getSize();

        houseService.findByUUID(created.getUuid());
        int cacheSizeAfterFind = cache.getSize();

        // then
        assertThat(cacheSizeAfterCreate).isEqualTo(1);
        assertThat(cacheSizeAfterFind).isEqualTo(1);

        verify(cache).getById(created.getUuid());
    }
}
