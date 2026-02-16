package com.celebration.infra.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataMessageJpaRepository extends JpaRepository<MessageJpaEntity, String> {

    Optional<MessageJpaEntity> findByPublicToken(String publicToken);
}
