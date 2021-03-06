package com.epam.esm.service.impl;

import com.epam.esm.dto.TagDto;
import com.epam.esm.entity.Tag;
import com.epam.esm.mapper.impl.TagMapper;
import com.epam.esm.repository.TagRepository;
import com.epam.esm.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The type Tag service.
 *
 * @author YanaV
 * @project GiftCertificate
 */
@Service
public class TagServiceImpl implements TagService {
    @Value("${max.result.amount}")
    private int maxResultAmount;
    private int lastPage;
    private final TagRepository repository;
    private final TagMapper mapper;

    /**
     * Instantiates a new Tag service.
     *
     * @param repository the repository
     * @param mapper     the mapper
     */
    @Autowired
    public TagServiceImpl(TagRepository repository, @Qualifier("tagServiceMapper") TagMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public boolean create(TagDto tagDto) {
        String name = tagDto.getName();
        if (!findByName(name).isPresent()) {
            Tag tag = new Tag();
            tag.setName(name);
            repository.save(tag);
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public boolean delete(long id) {
        boolean exists = repository.existsById(id);
        if (exists) {
            repository.deleteById(id);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Set<TagDto> findAll(int page) {
        Pageable pageable = PageRequest.of(page, maxResultAmount);
        Set<Tag> tags = repository.findAll(pageable).toSet();
        lastPage = repository.findAll(pageable).getTotalPages();
        return tags.stream()
                .map(mapper::mapToDto)
                .collect(Collectors.toSet());
    }

    @Override
    public Optional<TagDto> findById(long id) {
        Optional<Tag> tag = repository.findById(id);
        if (tag.isPresent()) {
            TagDto tagDto = mapper.mapToDto(tag.get());
            return Optional.of(tagDto);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<TagDto> findByName(String name) {
        Optional<Tag> tag = repository.findByName(name);
        if (tag.isPresent()) {
            TagDto tagDto = mapper.mapToDto(tag.get());
            return Optional.of(tagDto);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Set<TagDto> findMostUsedTag(int page) {
        Pageable pageable = PageRequest.of(page, maxResultAmount);
        lastPage = repository.findMostUsedTag(pageable).getTotalPages();
        return repository.findMostUsedTag(pageable)
                .stream()
                .map(mapper::mapToDto)
                .collect(Collectors.toSet());
    }

    @Override
    public int getLastPage() {
        return lastPage;
    }
}
