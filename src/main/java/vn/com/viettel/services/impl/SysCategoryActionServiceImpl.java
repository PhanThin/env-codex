package vn.com.viettel.services.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import vn.com.viettel.dto.SysCategoryActionDTO;
import vn.com.viettel.services.SysCategoryActionService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SysCategoryActionServiceImpl implements SysCategoryActionService {

    @PersistenceContext
    final EntityManager entityManager;

  @Override
  public List<SysCategoryActionDTO> getSysCategoryActionForExport(SysCategoryActionDTO dto) {
    String sqlWhere = " WHERE 1 = 1";
    if (!StringUtils.isEmpty(dto.getCodeLevel1())) sqlWhere += " AND sca.codeLevel1 like :codeLevel1";
    if (!StringUtils.isEmpty(dto.getCodeLevel2())) sqlWhere += " AND sca.codeLevel2 like :codeLevel2";
    if (!StringUtils.isEmpty(dto.getNameLevel1())) sqlWhere += " AND sca.nameLevel1 like :nameLevel1";
    if (!StringUtils.isEmpty(dto.getNameLevel2())) sqlWhere += " AND sca.nameLevel2 like :nameLevel2";
    if (!StringUtils.isEmpty(dto.getStatus())) sqlWhere += " AND sca.status = :status";
    if (!StringUtils.isEmpty(dto.getModifiedBy())) sqlWhere += " AND sca.modifiedBy = :modifiedBy";
    String sql = "SELECT * FROM (SELECT" +
      "    sca.id AS id," +
      "    scap.code AS codeLevel1," +
      "    scap.name AS nameLevel1," +
      "    sca.code AS codeLevel2," +
      "    sca.name AS nameLevel2," +
      "    sca.status," +
      "    sca.CREATED_DATE as createdDate," +
      "    sca.CREATED_BY as createdBy," +
      "    sca.MODIFIED_DATE as modifiedDate," +
      "    sca.MODIFIED_BY as modifiedBy" +
      " FROM SYS_CATEGORY_ACTION sca" +
      " LEFT JOIN SYS_CATEGORY_ACTION scap ON sca.PARENT_CODE = scap.CODE WHERE sca.PARENT_CODE IS NOT NULL) sca"
      + sqlWhere + " ORDER BY sca.createdDate DESC";
    Query query = entityManager.createNativeQuery(sql, "SysCategoryActionDTOMapping");
    if (!StringUtils.isEmpty(dto.getCodeLevel1())) query.setParameter("codeLevel1", "%" + dto.getCodeLevel1().trim() + "%");
    if (!StringUtils.isEmpty(dto.getNameLevel1())) query.setParameter("nameLevel1", "%" + dto.getNameLevel1().trim() + "%");
    if (!StringUtils.isEmpty(dto.getCodeLevel2())) query.setParameter("codeLevel2", "%" + dto.getCodeLevel2().trim() + "%");
    if (!StringUtils.isEmpty(dto.getNameLevel2())) query.setParameter("nameLevel2", "%" + dto.getNameLevel2().trim() + "%");
    if (!StringUtils.isEmpty(dto.getStatus())) query.setParameter("status", dto.getStatus());
    if (!StringUtils.isEmpty(dto.getModifiedBy())) query.setParameter("modifiedBy", dto.getModifiedBy());
    return query.getResultList();
  }
}
