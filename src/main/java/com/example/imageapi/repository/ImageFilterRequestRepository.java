package com.example.imageapi.repository;

import com.example.imageapi.domain.ImageFilterRequest;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 *  Image filter request repository.
 */
@Repository
public interface ImageFilterRequestRepository extends JpaRepository<ImageFilterRequest, Integer> {

    Optional<ImageFilterRequest> findByRequestId(String requestId);
}
