package com.example.financery.mapper;

import com.example.financery.dto.TagDtoRequest;
import com.example.financery.dto.TagDtoResponse;
import com.example.financery.model.Tag;
import org.springframework.stereotype.Component;

@Component
public class TagMapper {

    public TagDtoResponse toTagDto(Tag tag) {
        TagDtoResponse tagDtoResponse = new TagDtoResponse();
        tagDtoResponse.setId(tag.getId());
        tagDtoResponse.setTitle(tag.getTitle());
        tagDtoResponse.setUserId(tag.getUser().getId());
        return tagDtoResponse;
    }

    public Tag toTag(TagDtoRequest tagDtoRequest) {
        Tag tag = new Tag();
        tag.setTitle(tagDtoRequest.getTitle());
        return tag;
    }
}