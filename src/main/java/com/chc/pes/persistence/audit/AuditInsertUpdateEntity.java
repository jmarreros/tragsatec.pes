package com.chc.pes.persistence.audit;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
// import org.springframework.data.jpa.domain.support.AuditingEntityListener; // Igual que arriba

import java.time.LocalDateTime;

@MappedSuperclass
@Getter
@Setter
public class AuditInsertUpdateEntity {
    @Column(name = "created_at", nullable = false, updatable = false)
    @CreatedDate
    @JsonIgnore
    private LocalDateTime createdAt;

    @CreatedBy
    @JsonIgnore
    @Column(name = "created_by", nullable = false, updatable = false, length = 50)
    private String createdBy;

    @Column(name = "updated_at")
    @LastModifiedDate
    @JsonIgnore
    private LocalDateTime updatedAt;

    @LastModifiedBy
    @JsonIgnore
    @Column(name = "updated_by", length = 50)
    private String updatedBy;
}