package com.example.urlshortener.repository;

import java.util.Optional;

import com.example.urlshortener.domain.ShortLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ShortLinkRepository extends JpaRepository<ShortLink, Long> {

	boolean existsByCode(String code);

	Optional<ShortLink> findByCode(String code);

	Optional<ShortLink> findByCodeAndActiveTrue(String code);

	@Transactional
	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("update ShortLink link set link.clickCount = link.clickCount + 1 where link.code = :code and link.active = true")
	int incrementClickCount(@Param("code") String code);

	@Transactional
	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("update ShortLink link set link.active = false where link.code = :code")
	int deactivateByCode(@Param("code") String code);
}
