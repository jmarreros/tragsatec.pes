package com.chc.pes.persistence.repository.medicion;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.chc.pes.persistence.entity.medicion.ArchivoMedicionEntity;

@Repository
public interface ArchivoMedicionRepository extends JpaRepository<ArchivoMedicionEntity, Integer> {
}

