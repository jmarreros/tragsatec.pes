package com.chc.pes.persistence.repository.general;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import com.chc.pes.persistence.entity.general.EstacionUtEntity;

public interface EstacionUtRepository extends CrudRepository<EstacionUtEntity, Integer> {
    // Método para eliminar todas las asociaciones de una EstacionEntity por su ID
    @Modifying
    @Query("DELETE FROM EstacionUtEntity eut WHERE eut.estacion.id = :estacionId")
    void deleteByEstacionId(@Param("estacionId") Integer estacionId);

    // Método para eliminar todas las asociaciones de una UnidadTerritorialEntity por su ID
    @Modifying
    @Query("DELETE FROM EstacionUtEntity eut WHERE eut.unidadTerritorial.id = :unidadTerritorialId")
    void deleteByUnidadTerritorialId(@Param("unidadTerritorialId") Integer unidadTerritorialId);
}