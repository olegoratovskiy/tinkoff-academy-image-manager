package com.example.imageapi.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Image model.
 */
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "images")
public class Image {

    /**
     * Create image.
     *
     * @param name image name.
     * @param size image size.
     * @param userId user id.
     * @param fileId image id in storage.
     */
    public Image(String name, Long size, Long userId, String fileId) {
        this.name = name;
        this.size = size;
        this.fileId = fileId;
        this.userId = userId;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 100)
    private String name;

    private Long size;

    private long userId;

    @Column(length = 300)
    private String fileId;

}
