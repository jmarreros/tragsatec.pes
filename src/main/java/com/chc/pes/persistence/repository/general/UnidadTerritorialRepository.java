package com.chc.pes.persistence.repository.general;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.chc.pes.dto.general.UnidadTerritorialProjection;
import com.chc.pes.persistence.entity.general.UnidadTerritorialEntity;

import java.util.List;

public interface UnidadTerritorialRepository extends JpaRepository<UnidadTerritorialEntity, Integer> {

    @Query(value = "SELECT id, nombre, codigo FROM unidad_territorial WHERE tipo = :tipo ORDER BY codigo",
            nativeQuery = true)
    List<UnidadTerritorialProjection> findUnidadesTerritorialesByTipo(@Param("tipo") Character tipo);

    @Query(value = "SELECT id, nombre, codigo FROM unidad_territorial WHERE tipo = :tipo AND demarcacion_id = :demarcacion ORDER BY nombre",
            nativeQuery = true)
    List<UnidadTerritorialProjection> findUnidadesTerritorialesByTipoAndDemarcacion(@Param("tipo") Character tipo, @Param("demarcacion") Integer demarcacion);
}