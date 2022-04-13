package com.epam.esm.mapper.impl;

import com.epam.esm.dto.TagDto;
import com.epam.esm.entity.Tag;
import com.epam.esm.mapper.Mapper;
import org.springframework.stereotype.Service;

/**
 * The type Tag mapper.
 *
 * @author YanaV
 * @project GiftCertificate
 */
@Service("tagMapper")
public class TagMapper implements Mapper<Tag, TagDto> {
    @Override
    public TagDto mapToDto(Tag tag) {
        TagDto tagDto = new TagDto();
        tagDto.setId(tag.getId());
        tagDto.setName(tag.getName());
        return tagDto;
    }

    @Override
    public Tag mapToEntity(TagDto tagDto) {
        Tag tag = new Tag();
        tag.setId(tagDto.getId());
        tag.setName(tagDto.getName());
        return tag;
    }
}
