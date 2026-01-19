package vn.com.viettel.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.com.viettel.entities.CatManufacturer;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Sort;


import java.util.List;
import java.util.Optional;

public interface CatManufacturerRepository extends JpaRepository<CatManufacturer, Long>, JpaSpecificationExecutor<CatManufacturer> {

    List<CatManufacturer> findAllByIsDeletedFalse(Sort sort);

    @Query(value = "SELECT CAT_MANUFACTURER_SEQ.NEXTVAL FROM DUAL", nativeQuery = true)
    Long getNextId();

    Optional<CatManufacturer> findByManufacturerIdAndIsDeletedFalse(Long manufacturerId);

    @Query("select (count(m) > 0) from CatManufacturer m " +
            "where m.isDeleted = false and upper(trim(m.manufacturerCode)) = upper(trim(:code))")
    boolean existsActiveByCode(@Param("code") String code);

    @Query("select (count(m) > 0) from CatManufacturer m " +
            "where m.isDeleted = false and upper(trim(m.manufacturerName)) = upper(trim(:name))")
    boolean existsActiveByName(@Param("name") String name);

    @Query("select (count(m) > 0) from CatManufacturer m " +
            "where m.isDeleted = false and m.manufacturerId <> :id and upper(trim(m.manufacturerCode)) = upper(trim(:code))")
    boolean existsActiveByCodeAndNotId(@Param("id") Long id, @Param("code") String code);

    @Query("select (count(m) > 0) from CatManufacturer m " +
            "where m.isDeleted = false and m.manufacturerId <> :id and upper(trim(m.manufacturerName)) = upper(trim(:name))")
    boolean existsActiveByNameAndNotId(@Param("id") Long id, @Param("name") String name);
}
