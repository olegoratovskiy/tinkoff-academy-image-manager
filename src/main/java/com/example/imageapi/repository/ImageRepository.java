package com.example.imageapi.repository;

import com.example.imageapi.domain.Image;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 *  Image repository.
 */
@Repository
public interface ImageRepository extends JpaRepository<Image, Integer> {

    boolean existsByFileId(String fileId);

    void deleteByFileId(String fileId);

    Optional<Image> findImageByFileId(String fileId);

    List<Image> findAllByUserId(long userId);
}
