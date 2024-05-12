package com.example.imageapi.repository;

import com.example.imageapi.domain.HandlerIdempotency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 *  HandlerIdempotency repository.
 */
@Repository
public interface HandlerIdempotencyRepository extends JpaRepository<HandlerIdempotency, Integer> {

    boolean existsByRequestIdAndImageId(String requestId, String imageId);
}
