package com.epam.esm.service;

import com.epam.esm.dto.GiftCertificateDto;

import java.util.List;
import java.util.Map;

public interface GiftCertificateService extends BaseService<GiftCertificateDto> {
    boolean create(Map<String, String> certificateData);

    boolean update(Map<String, String> certificateData);

    List<GiftCertificateDto> findByName(String name);

    List<GiftCertificateDto> findByDescription(String description);
}