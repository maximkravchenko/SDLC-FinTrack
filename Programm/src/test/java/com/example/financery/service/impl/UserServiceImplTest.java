package com.example.financery.service.impl;

import com.example.financery.dto.UserDtoRequest;
import com.example.financery.dto.UserDtoResponse;
import com.example.financery.exception.NotFoundException;
import com.example.financery.mapper.UserMapper;
import com.example.financery.model.User;
import com.example.financery.repository.UserRepository;
import com.example.financery.utils.InMemoryCache;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private InMemoryCache cache;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserDtoRequest userDtoRequest;
    private UserDtoResponse userDtoResponse;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setBalance(0.0);
        user.setBills(new ArrayList<>());

        userDtoRequest = new UserDtoRequest();
        userDtoRequest.setName("Test User");
        userDtoRequest.setEmail("test@example.com");
        userDtoRequest.setBalance(100.0); // Будет проигнорировано

        userDtoResponse = new UserDtoResponse();
        userDtoResponse.setId(1L);
        userDtoResponse.setName("Test User");
        userDtoResponse.setEmail("test@example.com");
        userDtoResponse.setBalance(0.0);
        userDtoResponse.setBills(new ArrayList<>());
    }

    @Test
    void getAllUsers_success() {
        try (MockedStatic<Hibernate> mockHibernate = mockStatic(Hibernate.class)) {
            mockHibernate.when(() -> Hibernate.initialize(any())).thenAnswer(invocation -> null);

            when(userRepository.findAll()).thenReturn(List.of(user));
            when(userMapper.toDto(user)).thenReturn(userDtoResponse);

            List<UserDtoResponse> result = userService.getAllUsers();

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(userDtoResponse, result.get(0));
            verify(userRepository).findAll();
            verify(userMapper).toDto(user);
        }
    }

    @Test
    void getAllUsers_emptyList() {
        try (MockedStatic<Hibernate> mockHibernate = mockStatic(Hibernate.class)) {
            mockHibernate.when(() -> Hibernate.initialize(any())).thenAnswer(invocation -> null);

            when(userRepository.findAll()).thenReturn(Collections.emptyList());

            List<UserDtoResponse> result = userService.getAllUsers();

            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(userRepository).findAll();
        }
    }

    @Test
    void getAllUsers_hibernateInitializeThrowsException() {
        try (MockedStatic<Hibernate> mockHibernate = mockStatic(Hibernate.class)) {
            mockHibernate.when(() -> Hibernate.initialize(any()))
                    .thenThrow(new RuntimeException("Hibernate initialization failed"));

            when(userRepository.findAll()).thenReturn(List.of(user));

            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> userService.getAllUsers());

            assertEquals("Hibernate initialization failed", exception.getMessage());
            verify(userRepository).findAll();
            verify(userMapper, never()).toDto(any());
        }
    }

    @Test
    void getAllUsers_userMapperThrowsException() {
        try (MockedStatic<Hibernate> mockHibernate = mockStatic(Hibernate.class)) {
            mockHibernate.when(() -> Hibernate.initialize(any())).thenAnswer(invocation -> null);

            when(userRepository.findAll()).thenReturn(List.of(user));
            when(userMapper.toDto(user)).thenThrow(new RuntimeException("Mapping failed"));

            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> userService.getAllUsers());

            assertEquals("Mapping failed", exception.getMessage());
            verify(userRepository).findAll();
            verify(userMapper).toDto(user);
        }
    }

    @Test
    void createUser_success() {
        when(userMapper.toEntity(userDtoRequest)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(userDtoResponse);

        UserDtoResponse result = userService.createUser(userDtoRequest);

        assertNotNull(result);
        assertEquals(userDtoResponse, result);
        assertEquals(0.0, user.getBalance());
        verify(userMapper).toEntity(userDtoRequest);
        verify(userRepository).save(user);
        verify(userMapper).toDto(user);
    }

    @Test
    void createUser_saveThrowsException() {
        when(userMapper.toEntity(userDtoRequest)).thenReturn(user);
        when(userRepository.save(user)).thenThrow(new DataAccessException("Database error") {});

        DataAccessException exception = assertThrows(DataAccessException.class,
                () -> userService.createUser(userDtoRequest));

        assertEquals("Database error", exception.getMessage());
        verify(userMapper).toEntity(userDtoRequest);
        verify(userRepository).save(user);
        verify(userMapper, never()).toDto(any());
    }

    @Test
    void getUserById_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(userDtoResponse);

        UserDtoResponse result = userService.getUserById(1L);

        assertNotNull(result);
        assertEquals(userDtoResponse, result);
        verify(userRepository).findById(1L);
        verify(userMapper).toDto(user);
    }

    @Test
    void getUserById_userNotFound_throwsNotFoundException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userService.getUserById(1L));

        assertEquals("Пользователь с id 1 не найден", exception.getMessage());
        verify(userRepository).findById(1L);
        verify(userMapper, never()).toDto(any());
    }

    @Test
    void getUserByEmail_success() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(user);

        User result = userService.getUserByEmail("test@example.com");

        assertNotNull(result);
        assertEquals(user, result);
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void getUserByEmail_notFound() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(null);

        User result = userService.getUserByEmail("test@example.com");

        assertNull(result);
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void updateUser_success() {
        userDtoRequest.setName("Updated User");
        userDtoRequest.setEmail("updated@example.com");

        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setName("Updated User");
        updatedUser.setEmail("updated@example.com");
        updatedUser.setBalance(0.0);
        updatedUser.setBills(new ArrayList<>());

        UserDtoResponse updatedUserDtoResponse = new UserDtoResponse();
        updatedUserDtoResponse.setId(1L);
        updatedUserDtoResponse.setName("Updated User");
        updatedUserDtoResponse.setEmail("updated@example.com");
        updatedUserDtoResponse.setBalance(0.0);
        updatedUserDtoResponse.setBills(new ArrayList<>());

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(updatedUser);
        when(userMapper.toDto(updatedUser)).thenReturn(updatedUserDtoResponse);

        UserDtoResponse result = userService.updateUser(1L, userDtoRequest);

        assertNotNull(result);
        assertEquals(updatedUserDtoResponse, result);
        assertEquals("Updated User", user.getName());
        assertEquals("updated@example.com", user.getEmail());
        verify(userRepository).findById(1L);
        verify(userRepository).save(user);
        verify(userMapper).toDto(updatedUser);
    }

    @Test
    void updateUser_userNotFound_throwsNotFoundException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userService.updateUser(1L, userDtoRequest));

        assertEquals("Пользователь с id 1 не найден", exception.getMessage());
        verify(userRepository).findById(1L);
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_saveThrowsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenThrow(new DataAccessException("Database error") {});

        DataAccessException exception = assertThrows(DataAccessException.class,
                () -> userService.updateUser(1L, userDtoRequest));

        assertEquals("Database error", exception.getMessage());
        verify(userRepository).findById(1L);
        verify(userRepository).save(user);
        verify(userMapper, never()).toDto(any());
    }

    @Test
    void deleteUser_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.deleteUser(1L);

        verify(userRepository).findById(1L);
        verify(userRepository).deleteById(1L);
        verify(cache).clearForUser(1L);
    }

    @Test
    void deleteUser_userNotFound_throwsNotFoundException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userService.deleteUser(1L));

        assertEquals("Пользователь с id 1 не найден", exception.getMessage());
        verify(userRepository).findById(1L);
        verify(userRepository, never()).deleteById(anyLong());
        verify(cache, never()).clearForUser(anyLong());
    }

    @Test
    void deleteUser_cacheClearThrowsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        doThrow(new RuntimeException("Cache clear failed")).when(cache).clearForUser(1L);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.deleteUser(1L));

        assertEquals("Cache clear failed", exception.getMessage());
        verify(userRepository).findById(1L);
        verify(userRepository).deleteById(1L);
        verify(cache).clearForUser(1L);
    }
}