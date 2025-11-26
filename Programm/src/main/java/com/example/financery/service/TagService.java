package com.example.financery.service;

import com.example.financery.dto.TagDtoRequest;
import com.example.financery.dto.TagDtoResponse;
import com.example.financery.dto.TransactionDtoResponse;
import com.example.financery.model.Tag;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface TagService {
    List<TagDtoResponse> getAllTags();

    List<TagDtoResponse> getTagsByUserId(long userId);

    TagDtoResponse getTagById(long id);

    List<TagDtoResponse> getTagsByTransactionId(long transactionId);

    List<TransactionDtoResponse> getTransactionsByTagId(long tagId);

    List<Tag> saveAll(List<TagDtoRequest> tagList);

    TagDtoResponse createTag(TagDtoRequest tagDto);

    TagDtoResponse updateTag(long id, TagDtoRequest tagDto);

    void deleteTag(long id);
}
