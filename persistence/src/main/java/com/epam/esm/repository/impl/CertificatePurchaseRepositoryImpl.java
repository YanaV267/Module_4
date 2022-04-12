package com.epam.esm.repository.impl;

import com.epam.esm.entity.GiftCertificate;
import com.epam.esm.entity.Tag;
import com.epam.esm.repository.CertificatePurchaseRepository;
import com.epam.esm.repository.GiftCertificateRepository;
import com.epam.esm.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.util.Optional;

/**
 * The type Certificate purchase repository.
 *
 * @author YanaV
 * @project GiftCertificate
 */
@Repository
@Transactional
public class CertificatePurchaseRepositoryImpl implements CertificatePurchaseRepository {
    private static final String INSERT_PURCHASE = "INSERT INTO certificate_purchase (id_certificate, id_tag) VALUES (?, ?)";

    private final JdbcTemplate template;
    private final GiftCertificateRepository certificateRepository;
    private final TagRepository tagRepository;

    /**
     * Instantiates a new Certificate purchase repository.
     *
     * @param dataSource            the data source
     * @param certificateRepository the certificate repository
     * @param tagRepository         the tag repository
     */
    @Autowired
    public CertificatePurchaseRepositoryImpl(DataSource dataSource, GiftCertificateRepository certificateRepository,
                                             TagRepository tagRepository) {
        this.template = new JdbcTemplate(dataSource);
        this.certificateRepository = certificateRepository;
        this.tagRepository = tagRepository;
    }

    @Override
    public boolean create(GiftCertificate certificate) {
        long certificateId = certificateRepository.create(certificate);
        if (certificateId == 0) {
            return false;
        }
        certificate.getTags().forEach(tag -> {
            Optional<Tag> foundTag = tagRepository.findByName(tag.getName());
            long tagId = foundTag.map(Tag::getId).orElseGet(() -> tagRepository.create(tag));
            template.update(INSERT_PURCHASE, certificateId, tagId);
            tag.setId(tagId);
        });
        return certificate.getTags()
                .stream()
                .allMatch(t -> t.getId() != 0);
    }

    @Override
    public boolean update(GiftCertificate certificate) {
        Optional<GiftCertificate> foundCertificate = certificateRepository.findById(certificate.getId());
        if (foundCertificate.isPresent()) {
            certificateRepository.update(certificate);
            certificate.getTags().forEach(tag -> {
                Optional<Tag> foundTag = tagRepository.findByName(tag.getName());
                if (!foundTag.isPresent()) {
                    long tagId = tagRepository.create(tag);
                    template.update(INSERT_PURCHASE, certificate.getId(), tagId);
                    tag.setId(tagId);
                }
            });
            return certificate.getTags()
                    .stream()
                    .allMatch(t -> t.getId() != 0);
        } else {
            return false;
        }
    }
}
