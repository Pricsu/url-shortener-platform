package com.urlshortener.link_service;

import com.urlshortener.link_service.entity.Link;
import com.urlshortener.link_service.repository.LinkRepository;
import net.bytebuddy.utility.dispatcher.JavaDispatcher;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Testcontainers
public class LinkRepositoryIT {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:latest")
            .withExposedPorts(6379);


    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry){
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @DynamicPropertySource
    static void configureRedis(DynamicPropertyRegistry registry){
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);
        registry.add("spring.data.redis.host", redis::getHost);
    }

    @Autowired
    private LinkRepository linkRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Test
    void findByShortCode_savesCorrectly(){
        Link link = new Link();
        link.setShortCode("abc123");
        link.setOriginalUrl("oko.com");
        link.setOwnerId(1L);
        link.setCreatedAt(LocalDateTime.now());
        link.setClickCount(3L);

        linkRepository.save(link);

        Optional<Link> found = linkRepository.findByShortCode("abc123");

        assertThat(found).isPresent();
        assertThat(found.get().getOriginalUrl()).isEqualTo("oko.com");
    }

    @Test
    void findByShortCode_errorOnAlreadyExist(){
        Link link1 = new Link();
        link1.setShortCode("abc123");
        link1.setOriginalUrl("oko.com");
        link1.setOriginalUrl("oko.com");
        link1.setOwnerId(1L);
        link1.setCreatedAt(LocalDateTime.now());
        link1.setClickCount(3L);

        Link link2 = new Link();
        link2.setShortCode("abc123");
        link2.setOriginalUrl("oke.com");

        linkRepository.save(link1);


        assertThrows(DataIntegrityViolationException.class, () -> linkRepository.save(link2));
    }

    @Test
    void getOriginalUrl_RedisIncrement(){
        redisTemplate.opsForValue().set("count:somecode", "0",10, TimeUnit.MINUTES);
        redisTemplate.opsForValue().increment("count:somecode");
        redisTemplate.opsForValue().increment("count:somecode");
        redisTemplate.opsForValue().increment("count:somecode");

        assertThat(redisTemplate.opsForValue().get("count:somecode")).isEqualTo("3");
    }

    @Test
    void getOriginalUrl_RedisGetDel(){
        redisTemplate.opsForValue().set("count:somecode", "0");
        redisTemplate.opsForValue().set("url:somecode", "oko.com");

        String result1 = redisTemplate.opsForValue().getAndDelete("count:somecode");
        String result2 = redisTemplate.opsForValue().getAndDelete("url:somecode");

        assertThat(result1).isEqualTo("0");
        assertThat(result2).isEqualTo("oko.com");

        assertThat(redisTemplate.opsForValue().get("count:somecode")).isEqualTo(null);
        assertThat(redisTemplate.opsForValue().get("count:somecode")).isEqualTo(null);
    }
}
