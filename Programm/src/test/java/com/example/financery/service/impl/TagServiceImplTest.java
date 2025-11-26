package com.example.financery.service.impl;

import com.example.financery.dto.TagDtoRequest;
import com.example.financery.dto.TagDtoResponse;
import com.example.financery.dto.TransactionDtoResponse;
import com.example.financery.exception.InvalidInputException;
import com.example.financery.exception.NotFoundException;
import com.example.financery.mapper.TagMapper;
import com.example.financery.mapper.TransactionMapper;
import com.example.financery.model.Tag;
import com.example.financery.model.Transaction;
import com.example.financery.model.User;
import com.example.financery.repository.TagRepository;
import com.example.financery.repository.TransactionRepository;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TagServiceImplTest {

    @Mock
    private TagRepository tagRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TagMapper tagMapper;

    @Mock
    private TransactionMapper transactionMapper;

    @Mock
    private InMemoryCache cache;

    @InjectMocks
    private TagServiceImpl tagService;

    private Tag tag;
    private User user;
    private Transaction transaction;
    private TagDtoRequest tagDtoRequest;
    private TagDtoResponse tagDtoResponse;
    private TransactionDtoResponse transactionDtoResponse;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);

        tag = new Tag();
        tag.setId(1L);
        tag.setTitle("Test Tag");
        tag.setUser(user);
        tag.setTransactions(new ArrayList<>());

        transaction = new Transaction();
        transaction.setId(1L);
        transaction.setTags(new ArrayList<>(List.of(tag)));

        tagDtoRequest = new TagDtoRequest();
        tagDtoRequest.setTitle("Test Tag");
        tagDtoRequest.setUserId(1L);

        tagDtoResponse = new TagDtoResponse();
        tagDtoResponse.setId(1L);
        tagDtoResponse.setTitle("Test Tag");
        tagDtoResponse.setUserId(1L);

        transactionDtoResponse = new TransactionDtoResponse();
        transactionDtoResponse.setId(1L);
    }

    @Test
    void getAllTags_success() {
        when(tagRepository.findAll()).thenReturn(List.of(tag));
        when(tagMapper.toTagDto(tag)).thenReturn(tagDtoResponse);

        List<TagDtoResponse> result = tagService.getAllTags();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(tagDtoResponse, result.get(0));
        verify(tagRepository).findAll();
        verify(tagMapper).toTagDto(tag);
    }

    @Test
    void getAllTags_emptyList() {
        when(tagRepository.findAll()).thenReturn(Collections.emptyList());

        List<TagDtoResponse> result = tagService.getAllTags();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(tagRepository).findAll();
    }

    @Test
    void getTagsByUserId_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(tagRepository.findByUser(1L)).thenReturn(List.of(tag));
        when(tagMapper.toTagDto(tag)).thenReturn(tagDtoResponse);

        List<TagDtoResponse> result = tagService.getTagsByUserId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(tagDtoResponse, result.get(0));
        verify(userRepository).findById(1L);
        verify(tagRepository).findByUser(1L);
        verify(tagMapper).toTagDto(tag);
    }

    @Test
    void getTagsByUserId_userNotFound_throwsNotFoundException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> tagService.getTagsByUserId(1L));

        assertEquals("Пользователь с id 1 не найден", exception.getMessage());
        verify(userRepository).findById(1L);
        verify(tagRepository, never()).findByUser(anyLong());
    }

    @Test
    void getTagById_success() {
        when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));
        when(tagMapper.toTagDto(tag)).thenReturn(tagDtoResponse);

        TagDtoResponse result = tagService.getTagById(1L);

        assertNotNull(result);
        assertEquals(tagDtoResponse, result);
        verify(tagRepository).findById(1L);
        verify(tagMapper).toTagDto(tag);
    }

    @Test
    void getTagById_tagNotFound_throwsNotFoundException() {
        when(tagRepository.findById(1L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> tagService.getTagById(1L));

        assertEquals("Тег с id 1 не найден", exception.getMessage());
        verify(tagRepository).findById(1L);
    }

    @Test
    void getTagsByTransactionId_success() {
        when(tagRepository.findByTransaction(1L)).thenReturn(List.of(tag));
        when(tagMapper.toTagDto(tag)).thenReturn(tagDtoResponse);

        List<TagDtoResponse> result = tagService.getTagsByTransactionId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(tagDtoResponse, result.get(0));
        verify(tagRepository).findByTransaction(1L);
        verify(tagMapper).toTagDto(tag);
    }

    @Test
    void getTagsByTransactionId_emptyList() {
        when(tagRepository.findByTransaction(1L)).thenReturn(Collections.emptyList());

        List<TagDtoResponse> result = tagService.getTagsByTransactionId(1L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(tagRepository).findByTransaction(1L);
    }

    @Test
    void getTransactionsByTagId_success() {
        when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));
        when(tagRepository.findTransactionsByTag(1L)).thenReturn(List.of(transaction));
        when(transactionMapper.toTransactionDto(transaction)).thenReturn(transactionDtoResponse);

        List<TransactionDtoResponse> result = tagService.getTransactionsByTagId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(transactionDtoResponse, result.get(0));
        verify(tagRepository).findById(1L);
        verify(tagRepository).findTransactionsByTag(1L);
        verify(transactionMapper).toTransactionDto(transaction);
    }

    @Test
    void getTransactionsByTagId_tagNotFound_throwsNotFoundException() {
        when(tagRepository.findById(1L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> tagService.getTransactionsByTagId(1L));

        assertEquals("Тег с id 1 не найден", exception.getMessage());
        verify(tagRepository).findById(1L);
        verify(tagRepository, never()).findTransactionsByTag(anyLong());
    }

    @Test
    void saveAll_success() {
        TagDtoRequest invalidTag = new TagDtoRequest();
        invalidTag.setTitle("ab");
        invalidTag.setUserId(1L);
        List<TagDtoRequest> tagList = List.of(tagDtoRequest, invalidTag);

        when(userRepository.findAllById(List.of(1L))).thenReturn(List.of(user));
        when(tagMapper.toTag(tagDtoRequest)).thenReturn(tag);
        when(tagRepository.saveAll(any())).thenReturn(List.of(tag));

        List<Tag> result = tagService.saveAll(tagList);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(tag, result.get(0));
        verify(userRepository).findAllById(List.of(1L));
        verify(tagMapper).toTag(tagDtoRequest);
        verify(tagRepository).saveAll(any());
    }

    @Test
    void saveAll_userNotFound_throwsNotFoundException() {
        List<TagDtoRequest> tagList = List.of(tagDtoRequest);
        when(userRepository.findAllById(List.of(1L))).thenReturn(Collections.emptyList());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> tagService.saveAll(tagList));

        assertEquals("Один или несколько пользователей не найдены", exception.getMessage());
        verify(userRepository).findAllById(List.of(1L));
        verify(tagRepository, never()).saveAll(any());
    }

    @Test
    void createTag_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(tagMapper.toTag(tagDtoRequest)).thenReturn(tag);
        when(tagRepository.save(tag)).thenReturn(tag);
        when(tagMapper.toTagDto(tag)).thenReturn(tagDtoResponse);

        TagDtoResponse result = tagService.createTag(tagDtoRequest);

        assertNotNull(result);
        assertEquals(tagDtoResponse, result);
        verify(userRepository).findById(1L);
        verify(tagMapper).toTag(tagDtoRequest);
        verify(tagRepository).save(tag);
        verify(tagMapper).toTagDto(tag);
    }

    @Test
    void createTag_userNotFound_throwsNotFoundException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> tagService.createTag(tagDtoRequest));

        assertEquals("Пользователь с id 1 не найден", exception.getMessage());
        verify(userRepository).findById(1L);
        verify(tagRepository, never()).save(any());
    }

    @Test
    void updateTag_success() {
        tagDtoRequest.setTitle("Updated Tag");
        when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));
        when(tagRepository.findTransactionsByTag(1L)).thenReturn(Collections.emptyList());
        when(tagRepository.save(tag)).thenReturn(tag);
        when(tagMapper.toTagDto(tag)).thenReturn(tagDtoResponse);

        TagDtoResponse result = tagService.updateTag(1L, tagDtoRequest);

        assertNotNull(result);
        assertEquals(tagDtoResponse, result);
        verify(tagRepository, times(2)).findById(1L);
        verify(tagRepository).findTransactionsByTag(1L);
        verify(tagRepository).save(tag);
        verify(tagMapper).toTagDto(tag);
        verify(cache, never()).updateTransaction(anyLong(), any());
    }

    @Test
    void updateTag_withTransactions_success() {
        tagDtoRequest.setTitle("Updated Tag");
        when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));
        when(tagRepository.findTransactionsByTag(1L)).thenReturn(List.of(transaction));
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));
        when(transactionMapper.toTransactionDto(transaction)).thenReturn(transactionDtoResponse);
        when(tagRepository.save(tag)).thenReturn(tag);
        when(tagMapper.toTagDto(tag)).thenReturn(tagDtoResponse);

        TagDtoResponse result = tagService.updateTag(1L, tagDtoRequest);

        assertNotNull(result);
        assertEquals(tagDtoResponse, result);
        verify(tagRepository, times(2)).findById(1L);
        verify(tagRepository).findTransactionsByTag(1L);
        verify(transactionRepository).findById(1L);
        verify(transactionMapper, times(2)).toTransactionDto(transaction);
        verify(cache).updateTransaction(1L, transactionDtoResponse);
        verify(tagRepository).save(tag);
        verify(tagMapper).toTagDto(tag);
    }

    @Test
    void updateTag_tagNotFound_throwsNotFoundException() {
        when(tagRepository.findById(1L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> tagService.updateTag(1L, tagDtoRequest));

        assertEquals("Тег с id 1 не найден", exception.getMessage());
        verify(tagRepository).findById(1L);
    }

    @Test
    void updateTag_userMismatch_throwsInvalidInputException() {
        tagDtoRequest.setUserId(2L);
        when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));

        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> tagService.updateTag(1L, tagDtoRequest));

        assertEquals("Тег не принадлежит указанному пользователю", exception.getMessage());
        verify(tagRepository).findById(1L);
        verify(tagRepository, never()).save(any());
    }

    @Test
    void deleteTag_success() {
        when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));
        when(tagRepository.findTransactionsByTag(1L)).thenReturn(Collections.emptyList());
        when(transactionRepository.saveAll(Collections.emptyList()))
                .thenReturn(Collections.emptyList());

        tagService.deleteTag(1L);

        verify(tagRepository).findById(1L);
        verify(tagRepository).findTransactionsByTag(1L);
        verify(transactionRepository).saveAll(Collections.emptyList());
        verify(cache, never()).updateTransaction(anyLong(), any());
        verify(tagRepository).delete(tag);
    }

    @Test
    void deleteTag_withTransactions_success() {
        when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));
        when(tagRepository.findTransactionsByTag(1L)).thenReturn(List.of(transaction));
        when(transactionMapper.toTransactionDto(transaction)).thenReturn(transactionDtoResponse);
        when(transactionRepository.saveAll(any())).thenReturn(Collections.emptyList());

        // Мокаем Hibernate.initialize
        try (MockedStatic<Hibernate> mockHibernate = mockStatic(Hibernate.class)) {
            mockHibernate.when(() -> Hibernate.initialize(any())).thenAnswer(invocation -> null);

            tagService.deleteTag(1L);

            assertTrue(transaction.getTags().isEmpty());
            verify(tagRepository).findById(1L);
            verify(tagRepository).findTransactionsByTag(1L);
            verify(transactionRepository).saveAll(any());
            verify(cache).updateTransaction(1L, transactionDtoResponse);
            verify(tagRepository).delete(tag);
        }
    }

    @Test
    void deleteTag_tagNotFound_throwsNotFoundException() {
        when(tagRepository.findById(1L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> tagService.deleteTag(1L));

        assertEquals("Тег с id 1 не найден", exception.getMessage());
        verify(tagRepository).findById(1L);
        verify(tagRepository, never()).delete(any());
    }

    @Test
    void updateTag_transactionNotFound_throwsNotFoundException() {
        tagDtoRequest.setTitle("Updated Tag");
        when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));
        when(tagRepository.findTransactionsByTag(1L)).thenReturn(List.of(transaction));
        when(transactionMapper.toTransactionDto(transaction)).thenReturn(transactionDtoResponse);
        when(transactionRepository.findById(1L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> tagService.updateTag(1L, tagDtoRequest));

        assertEquals("Транзакция не найдена", exception.getMessage());
        verify(tagRepository, times(2)).findById(1L);
        verify(tagRepository).findTransactionsByTag(1L);
        verify(transactionMapper).toTransactionDto(transaction);
        verify(transactionRepository).findById(1L);
        verify(cache, never()).updateTransaction(anyLong(), any());
        verify(tagRepository).save(tag);
    }

    @Test
    void saveAll_nullTitle_filteredOut() {
        TagDtoRequest nullTitleTag = new TagDtoRequest();
        nullTitleTag.setTitle(null);
        nullTitleTag.setUserId(1L);
        List<TagDtoRequest> tagList = List.of(tagDtoRequest, nullTitleTag);

        when(userRepository.findAllById(List.of(1L))).thenReturn(List.of(user));
        when(tagMapper.toTag(tagDtoRequest)).thenReturn(tag);
        when(tagRepository.saveAll(any())).thenReturn(List.of(tag));

        List<Tag> result = tagService.saveAll(tagList);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(tag, result.get(0));
        verify(userRepository).findAllById(List.of(1L));
        verify(tagMapper).toTag(tagDtoRequest);
        verify(tagRepository).saveAll(any());
    }
}