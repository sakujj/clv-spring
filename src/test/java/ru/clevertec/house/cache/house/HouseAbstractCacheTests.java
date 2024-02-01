package ru.clevertec.house.cache.house;


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import ru.clevertec.house.cache.Cache;
import ru.clevertec.house.dto.HouseRequest;
import ru.clevertec.house.dto.HouseResponse;
import ru.clevertec.house.service.HouseService;
import ru.clevertec.house.test.util.HouseTestBuilder;
import ru.clevertec.house.testcontainer.ExclusivePostgresContainerInitializer;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@Transactional
public abstract class HouseAbstractCacheTests extends ExclusivePostgresContainerInitializer {

    private static final int THREAD_COUNT = 6;

    @SpyBean
    private Cache cache;

    private ExecutorService executorService;

    @SpyBean
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

        verify(houseService).findByUUID(any(UUID.class));
        verify(houseService, times(20)).update(any(HouseRequest.class), any(UUID.class));
    }


    @Test
    public void cacheShouldRemoveElementAfterDelete() {
        // given
        UUID uuid = UUID.fromString("e89895ef-ca4c-433b-87e8-3ead2646fed1");

        // when
        int cacheSizeInitial = cache.getSize();

        houseService.findByUUID(uuid);
        int cacheSizeAfterFirstFind = cache.getSize();

        houseService.deleteByUUID(uuid);
        int cacheSizeAfterDelete = cache.getSize();

        houseService.findByUUID(uuid);
        int cacheSizeAfterSecondFind = cache.getSize();

        // then
        assertThat(cacheSizeInitial).isEqualTo(0);
        assertThat(cacheSizeAfterFirstFind).isEqualTo(1);
        assertThat(cacheSizeAfterDelete).isEqualTo(0);
        assertThat(cacheSizeAfterSecondFind).isEqualTo(0);

        verify(cache, times(3)).getById(uuid);
        verify(cache).removeById(uuid);

        verify(houseService, times(2)).findByUUID(any(UUID.class));
        verify(houseService).deleteByUUID(any(UUID.class));
    }

    @Test
    public void cacheShouldContainElementAfterCreate() {
        // given
        HouseRequest houseRequest = HouseTestBuilder.aHouse().buildRequest();

        // when
        int cacheSizeInitial = cache.getSize();

        HouseResponse created = houseService.create(houseRequest);
        int cacheSizeAfterCreate = cache.getSize();

        houseService.findByUUID(created.getUuid());
        int cacheSizeAfterFind = cache.getSize();

        // then
        assertThat(cacheSizeInitial).isEqualTo(0);
        assertThat(cacheSizeAfterCreate).isEqualTo(1);
        assertThat(cacheSizeAfterFind).isEqualTo(1);

        verify(cache).getById(created.getUuid());

        verify(houseService).create(any(HouseRequest.class));
        verify(houseService, times(0)).findByUUID(any(UUID.class));
    }

    @Test
    public void cacheShouldHaveUpdatedElementAfterUpdate() {
        // given
        HouseRequest houseRequest = HouseTestBuilder.aHouse().buildRequest();
        UUID uuidToUpdate = UUID.fromString("acb8316d-3d13-4096-b1d6-f997b7307f0e");

        // when
        int cacheSizeInitial = cache.getSize();

        HouseResponse responseBeforeUpdate = houseService.findByUUID(uuidToUpdate).get();
        int cacheSizeAfterFirstFind = cache.getSize();

        houseService.update(houseRequest, uuidToUpdate);
        int cacheSizeAfterUpdate = cache.getSize();

        HouseResponse responseAfterUpdate = houseService.findByUUID(uuidToUpdate).get();
        int cacheSizeAfterSecondFind = cache.getSize();

        // then
        assertThat(cacheSizeInitial).isEqualTo(0);
        assertThat(cacheSizeAfterFirstFind).isEqualTo(1);
        assertThat(cacheSizeAfterUpdate).isEqualTo(1);
        assertThat(cacheSizeAfterSecondFind).isEqualTo(1);

        assertThat(responseAfterUpdate).isNotEqualTo(responseBeforeUpdate);

        verify(cache, atLeast(2)).getById(uuidToUpdate);

        verify(houseService).findByUUID(any(UUID.class));
        verify(houseService).update(any(HouseRequest.class), any(UUID.class));
    }


}
