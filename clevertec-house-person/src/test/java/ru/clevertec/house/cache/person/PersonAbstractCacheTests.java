package ru.clevertec.house.cache.person;

import io.github.sakujj.cache.Cache;
import io.github.sakujj.config.CacheAutoConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import ru.clevertec.house.dto.PersonRequest;
import ru.clevertec.house.dto.PersonResponse;
import ru.clevertec.house.entity.House;
import ru.clevertec.house.repository.HouseRepository;
import ru.clevertec.house.service.PersonService;
import ru.clevertec.house.test.util.PersonTestBuilder;
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

@Transactional
@SpringBootTest
@ImportAutoConfiguration(CacheAutoConfiguration.class)
public abstract class PersonAbstractCacheTests extends ExclusivePostgresContainerInitializer {

    private static final int THREAD_COUNT = 6;

    @SpyBean
    private Cache cache;

    private ExecutorService executorService;

    @SpyBean
    private PersonService personService;

    @Autowired
    private HouseRepository houseRepository;

    @AfterEach
    void clear() {
        cache.clear();
    }

    // can not test with update or create because (Persistence Context is different for
    // different threads) and (Person needs non-transient House instance, i.e. retrieved from Persistence Context)
    @Test
    public void cacheShouldStayTheSameSize() throws InterruptedException {
        // given
        executorService = Executors.newFixedThreadPool(THREAD_COUNT);

        PersonResponse personResponse = personService.findAll(PageRequest.of(0, 1))
                .getContent()
                .get(0);
        UUID uuid = personResponse.getUuid();

        CountDownLatch latch = new CountDownLatch(10);


        IntStream.range(0, 10).forEach(i -> executorService.submit(() ->
        {
            personService.findByUUID(uuid);
            latch.countDown();
        }));

        latch.await();
        executorService.shutdown();

        // when
        int actual = cache.getSize();

        // then
        assertThat(actual).isEqualTo(1);

        verify(cache, atLeast(10)).getById(uuid);

        verify(personService).findByUUID(any(UUID.class));
    }


    @Test
    public void cacheShouldRemoveElementAfterDelete() {
        // given
        UUID uuid = UUID.fromString("26df4783-5eae-4dd7-ae62-5249ea9c3c18");

        // when
        int cacheSizeInitial = cache.getSize();

        personService.findByUUID(uuid);
        int cacheSizeAfterFirstFind = cache.getSize();

        personService.deleteByUUID(uuid);
        int cacheSizeAfterDelete = cache.getSize();

        personService.findByUUID(uuid);
        int cacheSizeAfterSecondFind = cache.getSize();

        // then
        assertThat(cacheSizeInitial).isEqualTo(0);
        assertThat(cacheSizeAfterFirstFind).isEqualTo(1);
        assertThat(cacheSizeAfterDelete).isEqualTo(0);
        assertThat(cacheSizeAfterSecondFind).isEqualTo(0);

        verify(cache, times(3)).getById(uuid);
        verify(cache).removeById(uuid);

        verify(personService, times(2)).findByUUID(any(UUID.class));
        verify(personService).deleteByUUID(any(UUID.class));
    }

    @Test
    public void cacheShouldContainElementAfterCreate() {
        // given
        House house = houseRepository.findAll(PageRequest.of(0, 1))
                .getContent().get(0);

        PersonRequest personRequest = PersonTestBuilder.aPerson()
                .withHouseOfResidence(house)
                .buildRequest();

        // when
        int cacheSizeInitial = cache.getSize();

        PersonResponse created = personService.create(personRequest);
        int cacheSizeAfterCreate = cache.getSize();

        personService.findByUUID(created.getUuid());
        int cacheSizeAfterFind = cache.getSize();

        // then
        assertThat(cacheSizeInitial).isEqualTo(0);
        assertThat(cacheSizeAfterCreate).isEqualTo(1);
        assertThat(cacheSizeAfterFind).isEqualTo(1);

        verify(cache).getById(created.getUuid());

        verify(personService).create(any(PersonRequest.class));
        verify(personService, times(0)).findByUUID(any(UUID.class));
    }

    @Test
    public void cacheShouldHaveUpdatedElementAfterUpdate() {
        // given
        House house = houseRepository.findAll(PageRequest.of(0, 1))
                .getContent().get(0);

        PersonRequest personRequest = PersonTestBuilder.aPerson()
                .withHouseOfResidence(house)
                .buildRequest();
        UUID uuidToUpdate = UUID.fromString("26df4783-5eae-4dd7-ae62-5249ea9c3c18");

        // when
        int cacheSizeInitial = cache.getSize();

        PersonResponse responseBeforeUpdate = personService.findByUUID(uuidToUpdate).get();
        int cacheSizeAfterFirstFind = cache.getSize();

        personService.update(personRequest, uuidToUpdate);
        int cacheSizeAfterUpdate = cache.getSize();

        PersonResponse responseAfterUpdate = personService.findByUUID(uuidToUpdate).get();
        int cacheSizeAfterSecondFind = cache.getSize();

        // then
        assertThat(cacheSizeInitial).isEqualTo(0);
        assertThat(cacheSizeAfterFirstFind).isEqualTo(1);
        assertThat(cacheSizeAfterUpdate).isEqualTo(1);
        assertThat(cacheSizeAfterSecondFind).isEqualTo(1);

        assertThat(responseAfterUpdate).isNotEqualTo(responseBeforeUpdate);

        verify(cache, atLeast(2)).getById(uuidToUpdate);

        verify(personService).findByUUID(any(UUID.class));
        verify(personService).update(any(PersonRequest.class), any(UUID.class));
    }


}

