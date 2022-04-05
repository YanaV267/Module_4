package com.epam.esm.service;

import com.epam.esm.dto.TagDto;

import java.util.Optional;

/**
 * The interface Tag service.
 *
 * @author YanaV
 * @project GiftCertificate
 */
public interface TagService extends BaseService<TagDto> {
    /**
     * Find by name optional.
     *
     * @param name the name
     * @return the optional
     */
    Optional<TagDto> findByName(String name);
}
