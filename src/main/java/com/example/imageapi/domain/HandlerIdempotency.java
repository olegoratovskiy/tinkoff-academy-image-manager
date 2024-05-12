package com.example.imageapi.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Handler Idempotency model.
 */
@Data
@Entity
@NoArgsConstructor
@Accessors(chain = true)
@AllArgsConstructor
@Table(name = "handler_idempotency", uniqueConstraints = @UniqueConstraint(columnNames
    = {"requestId", "imageId"})
)
public class HandlerIdempotency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String requestId;

    @Column(nullable = false)
    private String imageId;

}
