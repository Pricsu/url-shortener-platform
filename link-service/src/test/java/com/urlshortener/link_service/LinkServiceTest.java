package com.urlshortener.link_service;

import com.urlshortener.link_service.dto.LinkRequest;
import com.urlshortener.link_service.dto.LinkResponse;
import com.urlshortener.link_service.dto.UrlResponse;
import com.urlshortener.link_service.entity.Link;
import com.urlshortener.link_service.repository.LinkRepository;
import com.urlshortener.link_service.security.UserPrincipal;
import com.urlshortener.link_service.service.LinkService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LinkServiceTest {

    @Mock
    private LinkRepository linkRepository;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String,String> valueOperations;

    private LinkService linkService;

    @BeforeEach
    void setUp(){
        linkService = new LinkService(linkRepository, redisTemplate);
    }

    @Test
    void shortCodeNotFound(){
//       Arrange
        when(linkRepository.findByShortCode("somecode")).thenReturn(Optional.empty());
//       Act and Assert
        assertThrows(IllegalArgumentException.class, () ->  linkService.getOriginalUrl("somecode"));
    }

    @Test
    void cacheMiss_populatesRedis(){

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        Link link = new Link();
        link.setOriginalUrl("oko@gmail.com");
        link.setShortCode("somecode");
        link.setClickLimit(null);

        when(linkRepository.findByShortCode("somecode")).thenReturn(Optional.of(link));
        when(valueOperations.get("url:somecode")).thenReturn(null);
        when(valueOperations.get("limit:somecode")).thenReturn("-");


        linkService.getOriginalUrl("somecode");

        verify(valueOperations).set("url:somecode", "oko@gmail.com", 10, TimeUnit.MINUTES);
        verify(valueOperations).set("limit:somecode", "-", 10, TimeUnit.MINUTES);
        verify(valueOperations).set("count:somecode", "0", 10, TimeUnit.MINUTES);
    }

    @Test
    void cacheHit_populatesRedis(){

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        Link link = new Link();
        link.setOriginalUrl("oko@gmail.com");
        link.setShortCode("somecode");
        link.setClickLimit(null);


        when(linkRepository.findByShortCode("somecode")).thenReturn(Optional.of(link));
        when(valueOperations.get("url:somecode")).thenReturn("oko@gmail.com");
        when(valueOperations.get("limit:somecode")).thenReturn("-");


        UrlResponse urlResponse = linkService.getOriginalUrl("somecode");
        verify(valueOperations, never()).set("url:somecode", "oko@gmail.com", 10, TimeUnit.MINUTES);
        verify(valueOperations, never()).set("limit:somecode", "-", 10, TimeUnit.MINUTES);

        assertThat(urlResponse.getOriginalUrl()).isEqualTo("oko@gmail.com");


    }

    @Test
    void atTheLimit(){

//        Arrange
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        Link link = new Link();
        link.setShortCode("somecode");
        link.setClickLimit(5L);
        link.setClickCount(1L);

        when(linkRepository.findByShortCode("somecode")).thenReturn(Optional.of(link));
        when(valueOperations.get("url:somecode")).thenReturn("oko@gmail.com");
        when(valueOperations.get("limit:somecode")).thenReturn(String.valueOf(5L));
        when(valueOperations.get("count:somecode")).thenReturn(String.valueOf(4L));

//        Act and Assert

        assertThrows(IllegalArgumentException.class, () -> linkService.getOriginalUrl("somecode"));
        verify(valueOperations, never()).increment("count:somecode");
    }

    @Test
    void belowTheLimit(){
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        Link link = new Link();
        link.setShortCode("somecode");
        link.setClickCount(1L);
        link.setClickLimit(5L);

        when(linkRepository.findByShortCode("somecode")).thenReturn(Optional.of(link));
        when(valueOperations.get("url:somecode")).thenReturn("oko@gmail.com");
        when(valueOperations.get("limit:somecode")).thenReturn(String.valueOf(5L));
        when(valueOperations.get("count:somecode")).thenReturn(String.valueOf(1L));

        UrlResponse urlResponse = linkService.getOriginalUrl("somecode");

        verify(valueOperations).increment("count:somecode");
        assertThat(urlResponse.getOriginalUrl()).isEqualTo("oko@gmail.com");
    }

    @Test
    void unlimitedCLickCount(){

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        Link link = new Link();
        link.setShortCode("somecode");
        link.setClickCount(10000L);

        when(linkRepository.findByShortCode("somecode")).thenReturn(Optional.of(link));
        when(valueOperations.get("url:somecode")).thenReturn("oko@gmail.com");
        when(valueOperations.get("limit:somecode")).thenReturn("-");


        UrlResponse urlResponse = linkService.getOriginalUrl("somecode");

        verify(valueOperations).increment("count:somecode");
        assertThat(urlResponse.getOriginalUrl()).isEqualTo("oko@gmail.com");
    }

    @Test
    void delete_usersAreNotEqual(){
        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            SecurityContext securityContext = mock(SecurityContext.class);
            Authentication authentication = mock(Authentication.class);
            UserPrincipal userPrincipal = new UserPrincipal("someUsername", 5L);

            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(userPrincipal);

            Link link = new Link();
            link.setOwnerId(2L);
            link.setId(1L);
            when(linkRepository.findById(1L)).thenReturn(Optional.of(link));

            assertThrows(AccessDeniedException.class, () -> linkService.deleteLink(1L));
        }
    }

    @Test
    void delete_usersAreEqual(){
        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)){

            SecurityContext securityContext = mock(SecurityContext.class);
            Authentication authentication = mock(Authentication.class);
            UserPrincipal userPrincipal = new UserPrincipal("someUsername", 5L);

            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(userPrincipal);

            Link link = new Link();
            link.setId(1L);
            link.setOwnerId(5L);
            link.setShortCode("somecode");
            when(linkRepository.findById(1L)).thenReturn(Optional.of(link));

            linkService.deleteLink(1L);

            verify(linkRepository,times(1)).delete(link);

            verify(redisTemplate).delete("count:somecode");
            verify(redisTemplate).delete("limit:somecode");
            verify(redisTemplate).delete("url:somecode");
        }
    }

    @Test
    void delete_linkNotFound(){
        when(linkRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> linkService.deleteLink(1L));
    }

    @Test
    void findMyLinks(){
        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {

            SecurityContext securityContext = mock(SecurityContext.class);
            Authentication authentication = mock(Authentication.class);
            UserPrincipal userPrincipal = new UserPrincipal("someUsername", 5L);

            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(userPrincipal);

            Link link = new Link();
            link.setOwnerId(5L);
            link.setShortCode("somecode");
            link.setOriginalUrl("oke@gmail.com");
            when(linkRepository.findByOwnerId(5L)).thenReturn(List.of(link));

            List<LinkResponse> result = linkService.findMyLinks();

            assertThat(result.size()).isEqualTo(1);
            assertThat(result.get(0).getOwnerId()).isEqualTo(5L);
            assertThat(result.get(0).getShortCode()).isEqualTo("somecode");
            assertThat(result.get(0).getOriginalUrl()).isEqualTo("oke@gmail.com");
        }
    }

    @Test
    void create_retriesOnCollision(){
        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {

            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            SecurityContext securityContext = mock(SecurityContext.class);
            Authentication authentication = mock(Authentication.class);
            UserPrincipal userPrincipal = new UserPrincipal("someUsername", 5L);

            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(userPrincipal);

            when(linkRepository.findByShortCode(anyString())).thenReturn(Optional.of(new Link())).thenReturn(Optional.empty());
            LinkRequest request = new LinkRequest();
            request.setOriginalUrl("oko@gmail.com");
            request.setClickLimit(5L);

            linkService.create(request);

            verify(linkRepository, times(2)).findByShortCode(anyString());
        }
    }

    @Test
    void create_savesLinkAndPopulatesRedis(){

        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {

//            Arrange
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            SecurityContext securityContext = mock(SecurityContext.class);
            Authentication authentication = mock(Authentication.class);
            UserPrincipal userPrincipal = new UserPrincipal("someUsername", 5L);

            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(userPrincipal);

            when(linkRepository.findByShortCode(anyString())).thenReturn(Optional.empty());
            LinkRequest request = new LinkRequest();
            request.setOriginalUrl("oko@gmail.com");
            request.setClickLimit(5L);

//            when(valueOperations.get("url:somecode")).thenReturn("oko@gmail.com");
//            when(valueOperations.get("limit:somecode")).thenReturn(String.valueOf(5L));

//            Act
            LinkResponse response = linkService.create(request);

//            Assert

            ArgumentCaptor<Link> linkCaptor = ArgumentCaptor.forClass(Link.class);
            verify(linkRepository).save(linkCaptor.capture());
            Link savedLink = linkCaptor.getValue();


            assertThat(response.getOriginalUrl()).isEqualTo("oko@gmail.com");

            verify(valueOperations).set(anyString(), eq("oko@gmail.com"), eq(10L), eq(TimeUnit.MINUTES));
            verify(valueOperations).set(anyString(), eq("0"), eq(10L), eq(TimeUnit.MINUTES));
            verify(valueOperations).set(anyString(), eq("5"), eq(10L), eq(TimeUnit.MINUTES));
        }
    }
}
