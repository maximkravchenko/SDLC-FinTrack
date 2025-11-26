package com.example.financery.service.impl;

import com.example.financery.dto.TagDtoRequest;
import com.example.financery.dto.TagDtoResponse;
import com.example.financery.dto.TransactionDtoResponse;
import com.example.financery.exception.AlreadyExistsException;
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
import com.example.financery.service.TagService;
import com.example.financery.utils.InMemoryCache;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class TagServiceImpl implements TagService {

    public static final String TAG_WITH_ID_NOT_FOUND = "Тег с id %d не найден";

    private final TagRepository tagRepository;
    private final TagMapper tagMapper;
    private final TransactionMapper transactionMapper;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    private final InMemoryCache cache;

    @Override
    public List<TagDtoResponse> getAllTags() {
        List<TagDtoResponse> tagsResponse = new ArrayList<>();
        tagRepository.findAll().forEach(
                tag -> tagsResponse.add(tagMapper.toTagDto(tag)));
        return tagsResponse;
    }

    @Override
    public List<TagDtoResponse> getTagsByUserId(long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Пользователь с id %d не найден", userId)));
        List<Tag> tags = tagRepository.findByUser(userId);
        List<TagDtoResponse> tagsResponse = new ArrayList<>();
        tags.forEach(tag -> tagsResponse.add(tagMapper.toTagDto(tag)));
        return tagsResponse;
    }

    @Override
    public TagDtoResponse getTagById(long id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        String.format(TAG_WITH_ID_NOT_FOUND, id)));
        return tagMapper.toTagDto(tag);
    }

    @Override
    public List<TagDtoResponse> getTagsByTransactionId(long transactionId) {
        List<Tag> tags = tagRepository.findByTransaction(transactionId);
        List<TagDtoResponse> tagsResponse = new ArrayList<>();
        tags.forEach(tag -> tagsResponse.add(tagMapper.toTagDto(tag)));
        return tagsResponse;
    }

    @Override
    public List<TransactionDtoResponse> getTransactionsByTagId(long tagId) {
        tagRepository.findById(tagId)
                .orElseThrow(() -> new NotFoundException(
                        String.format(TAG_WITH_ID_NOT_FOUND, tagId)));
        List<TransactionDtoResponse> transactionsResponse = new ArrayList<>();
        tagRepository.findTransactionsByTag(tagId)
                .forEach(transaction -> transactionsResponse
                        .add(transactionMapper.toTransactionDto(transaction)));
        return transactionsResponse;
    }

    @Override
    public List<Tag> saveAll(List<TagDtoRequest> tagList) {
        List<Long> userIds = tagList.stream()
                .map(TagDtoRequest::getUserId)
                .distinct()
                .toList();
        List<User> users = userRepository.findAllById(userIds);
        if (users.size() != userIds.size()) {
            throw new NotFoundException("Один или несколько пользователей не найдены");
        }
        Map<Long, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        List<Tag> tags = tagList.stream()
                .filter(dto -> dto.getTitle() != null && dto.getTitle().length() >= 3)
                .map(dto -> {
                    Tag tag = tagMapper.toTag(dto);
                    tag.setUser(userMap.get(dto.getUserId()));
                    return tag;
                })
                .toList();
        return tagRepository.saveAll(tags);
    }

    @Override
    public TagDtoResponse createTag(TagDtoRequest tagDto) {
        User user = userRepository.findById(tagDto.getUserId())
                .orElseThrow(() -> new NotFoundException(
                        "Пользователь с id "
                        + tagDto.getUserId()
                        + " не найден"));

        Tag tag = tagMapper.toTag(tagDto);
        tag.setUser(user);
        tagRepository.save(tag);

        return tagMapper.toTagDto(tag);
    }

    @Override
    public TagDtoResponse updateTag(long id, TagDtoRequest tagDto) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        String.format(TAG_WITH_ID_NOT_FOUND, id)));
        Long userId = tag.getUser().getId();

        if (tag.getUser().getId() != tagDto.getUserId()) {
            throw new InvalidInputException("Тег не принадлежит указанному пользователю");
        }

        tag.setTitle(tagDto.getTitle());
        tagRepository.save(tag);

        List<TransactionDtoResponse> transactions = getTransactionsByTagId(id);
        for (TransactionDtoResponse transaction : transactions) {
            cache.updateTransaction(userId, transactionMapper.toTransactionDto(
                    transactionRepository
                            .findById(transaction
                                    .getId())
                            .orElseThrow(() -> new NotFoundException(
                                    "Транзакция не найдена"))));
        }

        return tagMapper.toTagDto(tag);
    }

    @Override
    @Transactional
    public void deleteTag(long id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        String.format(TAG_WITH_ID_NOT_FOUND, id)));
        Long userId = tag.getUser().getId();

        List<Transaction> transactions = tagRepository.findTransactionsByTag(id);
        List<TransactionDtoResponse> transactionDtos = new ArrayList<>();

        for (Transaction transaction : transactions) {
            Hibernate.initialize(transaction.getTags());
            transaction.getTags().removeIf(t -> t.getId().equals(id));
            transactionDtos.add(transactionMapper.toTransactionDto(transaction));
        }

        transactionRepository.saveAll(transactions);

        for (TransactionDtoResponse transaction : transactionDtos) {
            cache.updateTransaction(userId, transaction);
        }

        tagRepository.delete(tag);
    }
}