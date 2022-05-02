package com.epam.esm.repository;

import com.epam.esm.entity.GiftCertificate;

import java.util.List;
import java.util.Set;

/**
 * The interface Gift certificate repository.
 *
 * @author YanaV
 * @project GiftCertificate
 */
public interface GiftCertificateRepository extends CompleteBaseRepository<GiftCertificate> {
    /**
     * Find by several parameters set.
     *
     * @param firstElementNumber the first element number
     * @param certificate        the certificate
     * @param sortTypes          the sort types
     * @return the set
     */
    Set<GiftCertificate> findBySeveralParameters(int firstElementNumber, GiftCertificate certificate,
                                                 List<String> sortTypes);
}
