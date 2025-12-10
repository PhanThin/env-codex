package vn.com.viettel.services.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.com.viettel.dto.DocInternalDTO;
import vn.com.viettel.dto.SysCategoryDTO;
import vn.com.viettel.dto.UploadMessageDTO;
import vn.com.viettel.entities.DocInternal;
import vn.com.viettel.entities.SysCategory;
import vn.com.viettel.repositories.jpa.DocInternalRepositoryJPA;
import vn.com.viettel.repositories.jpa.SysCategoryRepositoryJPA;
import vn.com.viettel.services.DocInternalService;
import vn.com.viettel.utils.excel.ExcelHelpers;
import vn.com.viettel.utils.exceptions.CustomException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DocInternalServiceImpl implements DocInternalService {
    final DocInternalRepositoryJPA docInternalRepositoryJPA;
    final SysCategoryRepositoryJPA sysCategoryRepositoryJPA;

    @PersistenceContext
    final EntityManager entityManager;

    @Override
    public List<DocInternalDTO> getDocInternalsForExport(DocInternalDTO dto) {
        if (dto.getIssueDateFrom() != null && dto.getIssueDateTo() != null && dto.getIssueDateFrom().after(dto.getIssueDateTo())) {
            throw new CustomException("Ngày ban hành từ ngày phải nhở hơn đến ngày");
        }

        if (dto.getEffectiveDateFrom() != null && dto.getEffectiveDateTo() != null && dto.getEffectiveDateFrom().after(dto.getEffectiveDateTo())) {
            throw new CustomException("Ngày hiệu lực từ ngày phải nhở hơn đến ngày");
        }

        if (dto.getExpireDateFrom() != null && dto.getExpireDateTo() != null && dto.getExpireDateFrom().after(dto.getExpireDateTo())) {
            throw new CustomException("Ngày hết hạn từ ngày phải nhở hơn đến ngày");
        }
        String sqlWhere = generateSqlWhere(dto);
        String sql = "SELECT new vn.com.viettel.dto.DocInternalDTO(di.code, di.title, t.name, org.name, topic.name, di.issuedDate, di.effectiveDate, di.effectiveStatus, di.expireDate) ";
        sql += " FROM DocInternal di ";
        sql += " LEFT JOIN SysCategory t ON di.type = t.id";
        sql += " LEFT JOIN SysCategory org ON di.orgOwn = org.id";
        sql += " LEFT JOIN SysCategory topic ON di.topic = topic.id";
        sql += sqlWhere + " ORDER BY di.createdDate DESC";
        TypedQuery<DocInternalDTO> query = entityManager.createQuery(sql, DocInternalDTO.class);
        setParameterForExport(dto, query);
        return query.getResultList();
    }

    @Override
    public DocInternal createDocInternal(DocInternalDTO dto, MultipartFile[] files) {
        handleValidate(dto);
        if (docInternalRepositoryJPA.existsByCodeAndIsDelete(dto.getCode().trim(), DocInternal.IsDelete.NO.value))
            throw new CustomException("Văn bản " + dto.getCode() + " đã tồn tại");
        DocInternal docInternal = dto.toEntity();
        docInternal.setCreatedType(DocInternal.CreatedType.MANUAL.value);
        docInternal.setIsDelete(DocInternal.IsDelete.NO.value);
        docInternalRepositoryJPA.save(docInternal);
        return docInternal;
    }

    @Override
    public DocInternal updateDocInternal(DocInternalDTO dto, MultipartFile[] files) {
        handleValidate(dto);
        Optional<DocInternal> docInternalOpt = docInternalRepositoryJPA.findById(dto.getId());
        if (docInternalOpt.isEmpty())
            throw new CustomException("Không tồn tại mã văn bản nội bộ " + dto.getCode());
        DocInternal docInternal = docInternalOpt.get();
        docInternal.setCode(dto.getCode().trim());
        docInternal.setTitle(dto.getTitle().trim());
        docInternal.setType(dto.getType());
        docInternal.setOrgOwn(dto.getOrgOwn());
        docInternal.setTopic(dto.getTopic());
        docInternal.setIssuedDate(new java.sql.Date(dto.getIssuedDate().getTime()));
        docInternal.setEffectiveDate(new java.sql.Date(dto.getEffectiveDate().getTime()));
        if (dto.getExpireDate() != null)
            docInternal.setExpireDate(new java.sql.Date(dto.getExpireDate().getTime()));
        docInternal.setEffectiveStatus(dto.getEffectiveStatus());
        docInternalRepositoryJPA.save(docInternal);
        return docInternal;
    }

    @Override
    public UploadMessageDTO importDocInternals(MultipartFile file) throws IOException {
        byte[] byteArr = file.getBytes();
        String fileName = file.getOriginalFilename();
        assert fileName != null;
        byte[] bytes;
        UploadMessageDTO message = new UploadMessageDTO();
        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(byteArr))) {
            Sheet sheet = workbook.getSheetAt(0);
            // Validate match template
            boolean isMatchTemplate = true;
            String fileNameTemplate = "template_thu_vien_VBNB.xlsx";
            File fileTemplate = new File("" + File.separator + fileNameTemplate);
            try (Workbook workbookTemplate = new XSSFWorkbook(new FileInputStream(fileTemplate))) {
                Sheet sheetTemplate = workbookTemplate.getSheetAt(0);
                if (!ExcelHelpers.rowsAreEqual(sheetTemplate.getRow(0), sheet.getRow(0))
                        || !ExcelHelpers.rowsAreEqual(sheetTemplate.getRow(1), sheet.getRow(1))
                        || !ExcelHelpers.rowsAreEqual(sheetTemplate.getRow(2), sheet.getRow(2))) {
                    isMatchTemplate = false;
                }
            }
            if (!isMatchTemplate) throw new CustomException("Sai định dạng với template");

            int totalRows = sheet.getPhysicalNumberOfRows();
            log.info("importDocInternals Sheet row " + totalRows);
            if (totalRows > 1003) throw new CustomException("Tối đa tổng số bản ghi import là 1000");
            int totalInsert = 0;
            int totalUpdate = 0;
            int totalError = 0;
            List<SysCategoryDTO> sysCategories = sysCategoryRepositoryJPA.getAllSysCategories();
            for (int i = 3; i <= sheet.getLastRowNum(); i++) {
                if (ExcelHelpers.hasEmptyAllCellOnRow(sheet.getRow(i), 0, 12)) {
                    Row row = sheet.getRow(i);
                    if (validateFileImport(row)) {
                        if (handleRowImport(sysCategories, row)) {
                            String type = ExcelHelpers.getStringCellValue(row.getCell(3));
                            String org = ExcelHelpers.getStringCellValue(row.getCell(5));
                            String topic = ExcelHelpers.getStringCellValue(row.getCell(7));
                            Optional<SysCategoryDTO> typeOpt = sysCategories.stream()
                                    .filter(o -> Objects.equals(o.getCode(), type))
                                    .filter(o -> o.getType() == SysCategory.Type.LOAI_VB.value)
                                    .findFirst();
                            Optional<SysCategoryDTO> orgOpt = sysCategories.stream()
                                    .filter(o -> Objects.equals(o.getCode(), org))
                                    .filter(o -> o.getType() == SysCategory.Type.DV_SO_HUU.value)
                                    .findFirst();
                            Optional<SysCategoryDTO> topicOpt = sysCategories.stream()
                                    .filter(o -> Objects.equals(o.getCode(), topic))
                                    .filter(o -> o.getType() == SysCategory.Type.TOPIC.value)
                                    .findFirst();
                            DocInternal docInternal = new DocInternal();
                            docInternal.setCode(ExcelHelpers.getStringCellValue(row.getCell(0)));
                            docInternal.setTitle(ExcelHelpers.getStringCellValue(row.getCell(1)));
                            docInternal.setType(typeOpt.get().getId());
                            docInternal.setOrgOwn(orgOpt.get().getId());
                            docInternal.setTopic(topicOpt.get().getId());
                            docInternal.setIssuedDate(new java.sql.Date(row.getCell(8).getDateCellValue().getTime()));
                            docInternal.setEffectiveDate(new java.sql.Date(row.getCell(9).getDateCellValue().getTime()));
                            docInternal.setEffectiveStatus(ExcelHelpers.getStringCellValue(row.getCell(10)));
                            if (!StringUtils.isEmpty(ExcelHelpers.getStringCellValue(row.getCell(11))))
                                docInternal.setExpireDate(new java.sql.Date(row.getCell(11).getDateCellValue().getTime()));
                            if (Objects.equals(ExcelHelpers.getStringCellValue(row.getCell(12)), "1")) {
                                docInternal.setCreatedType(DocInternal.CreatedType.MANUAL.value);
                                docInternal.setIsDelete(DocInternal.IsDelete.NO.value);
                                docInternalRepositoryJPA.save(docInternal);
                                totalInsert++;
                            } else if (Objects.equals(ExcelHelpers.getStringCellValue(row.getCell(12)), "2")) {
                                docInternalRepositoryJPA.save(docInternal);
                                totalUpdate++;
                            }
                        } else {
                            totalError++;
                        }
                    } else {
                        totalError++;
                    }
                }
            }
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            workbook.write(os);
            bytes = os.toByteArray();

            if (totalError > 0) {
                String dataExport = Base64.getEncoder().encodeToString(bytes);
                message.setMessageCode("001");
                message.setMessageDesc("Thêm mới: " + totalInsert + " | Cập nhật: " + totalUpdate + " | Lỗi: " + totalError);
                message.setFileData(dataExport);
            } else {
                message.setMessageCode("000");
                message.setMessageDesc("Thêm mới: " + totalInsert + " | Cập nhật: " + totalUpdate);
            }
        } catch (Exception e) {
            log.error("Import noi dung khong thanh cong ", e);
            if (e instanceof CustomException) throw new CustomException(e.getMessage());
        }
        return message;
    }

    @Override
    public UploadMessageDTO validateImportDocInternals(MultipartFile file) throws IOException {
        byte[] byteArr = file.getBytes();
        String fileName = file.getOriginalFilename();
        assert fileName != null;
        byte[] bytes;
        UploadMessageDTO message = new UploadMessageDTO();
        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(byteArr))) {
            Sheet sheet = workbook.getSheetAt(0);

            // Validate match template
            boolean isMatchTemplate = true;
            String fileNameTemplate = "template_thu_vien_VBNB.xlsx";
            File fileTemplate = new File("" + File.separator + fileNameTemplate);
            try (Workbook workbookTemplate = new XSSFWorkbook(new FileInputStream(fileTemplate))) {
                Sheet sheetTemplate = workbookTemplate.getSheetAt(0);
                if (!ExcelHelpers.rowsAreEqual(sheetTemplate.getRow(0), sheet.getRow(0))
                        || !ExcelHelpers.rowsAreEqual(sheetTemplate.getRow(1), sheet.getRow(1))
                        || !ExcelHelpers.rowsAreEqual(sheetTemplate.getRow(2), sheet.getRow(2))) {
                    isMatchTemplate = false;
                }
            }
            if (!isMatchTemplate) throw new CustomException("Sai định dạng với template");

            int totalRows = sheet.getPhysicalNumberOfRows();
            log.info("validateImportDocInternals Sheet row " + totalRows);
            if (totalRows > 1003) throw new CustomException("Tối đa tổng số bản ghi import là 1000");
            int totalError = 0;
            for (int i = 3; i <= sheet.getLastRowNum(); i++) {
                if (ExcelHelpers.hasEmptyAllCellOnRow(sheet.getRow(i), 0, 11)) {
                    Row row = sheet.getRow(i);
                    if (validateFileImport(row)) {
                        writeResult(row, "Success", "Thành công");
                    } else {
                        totalError++;
                    }
                }
            }
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            workbook.write(os);
            bytes = os.toByteArray();

            if (totalError > 0) {
                String dataExport = Base64.getEncoder().encodeToString(bytes);
                message.setMessageCode("001");
                message.setMessageDesc("Validate file thành không. Có " + totalError + " bản ghi lỗi");
                message.setFileData(dataExport);
            } else {
                message.setMessageCode("000");
                message.setMessageDesc("Validate file thành không. Không có bản ghi lỗi");
            }
        } catch (Exception e) {
            log.error("Import noi dung khong thanh cong ", e);
            if (e instanceof CustomException) throw new CustomException(e.getMessage());
        }
        return message;
    }

    @Override
    public Page<DocInternalDTO> getDocInternalsForBuilding(DocInternalDTO dto) {
        int page = dto.getPage() - 1;
        if (page < 0) page = 0;
        PageRequest pageable = PageRequest.of(page, 20);
        String sqlWhere = " WHERE 1 = 1", sqlWhere2 = " WHERE 1 = 1 ";
        if (!StringUtils.isEmpty(dto.getCode())) sqlWhere += " AND LOWER(di.CODE) like :code";
        if (!StringUtils.isEmpty(dto.getTitle())) sqlWhere += " AND LOWER(di.TITLE) like :title";
        if (Objects.equals(dto.getMappingStatus(), "0")) sqlWhere2 += " AND di.totalMapping = 0";
        if (Objects.equals(dto.getMappingStatus(), "1")) sqlWhere2 += " AND di.totalMapping > 0";

        if (Objects.equals(dto.getBuildingStatus(), "0")) sqlWhere += " AND dio.ID is null";
        if (Objects.equals(dto.getBuildingStatus(), "1")) sqlWhere += " AND dio.ID is not null";
        String sql = "SELECT di.ID, di.CODE, di.TITLE, di.EFFECTIVE_STATUS as effectiveStatus, 0 as totalNV," +
                "       SUM(di.totalMapping) AS totalMapping, SUM(di.totalApproved) AS totalApproved" +
                " FROM (" +
                "    SELECT di.ID, di.CODE, di.TITLE, di.EFFECTIVE_STATUS," +
                "    CASE WHEN dio.STATUS_MAPPING = 1 THEN 1 ELSE 0 END  totalMapping," +
                "    CASE WHEN dio.STATUS_APPROVAL = 1 THEN 1 ELSE 0 END totalApproved" +
                "    FROM DOC_INTERNAL di" +
                "    LEFT JOIN DOC_INTERNAL_OBLIGATIONS dio on di.ID = dio.DOC_INTERNAL_ID" + sqlWhere +
                " ) di" + sqlWhere2 +
                " GROUP BY di.ID, di.CODE, di.TITLE, di.EFFECTIVE_STATUS ORDER BY di.ID DESC";
        String sqlCount = "SELECT count(*) from (" + sql + ") di ";

        Query query = entityManager.createNativeQuery(sql, "DocInternalDTOMapping")
                .setFirstResult(pageable.getPageNumber() * pageable.getPageSize())
                .setMaxResults(pageable.getPageSize());
        Query queryCount = entityManager.createNativeQuery(sqlCount);
        if (!StringUtils.isEmpty(dto.getCode())) {
            query.setParameter("code", "%" + dto.getCode().trim().toLowerCase() + "%");
            queryCount.setParameter("code", "%" + dto.getCode().trim().toLowerCase() + "%");
        }
        if (!StringUtils.isEmpty(dto.getTitle())) {
            query.setParameter("title", "%" + dto.getTitle().trim().toLowerCase() + "%");
            queryCount.setParameter("title", "%" + dto.getTitle().trim().toLowerCase() + "%");
        }
        BigDecimal totalRecord = (BigDecimal) queryCount.getSingleResult();
        List<DocInternalDTO> results = query.getResultList();
        return new PageImpl<>(results, pageable, totalRecord.intValue());
    }

    private boolean handleValidate(DocInternalDTO dto) {
        if (Objects.equals(dto.getEffectiveStatus(), "0") && dto.getExpireDate() == null)
            throw new CustomException("Ngày hiệu lực không được để trống");
        Calendar today = Calendar.getInstance();
        today.setTime(new Date());
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        if (dto.getIssuedDate().after(today.getTime())) throw new CustomException("Ngày ban hành không hợp lệ");
        if (dto.getEffectiveDate().before(dto.getIssuedDate()))
            throw new CustomException("Ngày hiệu lực không hợp lệ");
        if (Objects.equals(dto.getEffectiveStatus(), "0") && dto.getExpireDate().before(dto.getEffectiveDate()))
            throw new CustomException("Ngày hết hiệu lực không hợp lệ");
        return true;
    }

    private boolean handleRowImport(List<SysCategoryDTO> sysCategories, Row row) {
        Calendar today = Calendar.getInstance();
        today.setTime(new Date());
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        String msg = "";
        try {
            String type = ExcelHelpers.getStringCellValue(row.getCell(3));
            String org = ExcelHelpers.getStringCellValue(row.getCell(5));
            String topic = ExcelHelpers.getStringCellValue(row.getCell(7));
            Optional<SysCategoryDTO> typeOpt = sysCategories.stream()
                    .filter(o -> Objects.equals(o.getCode(), type))
                    .filter(o -> o.getType() == SysCategory.Type.LOAI_VB.value)
                    .findFirst();
            if (typeOpt.isEmpty()) msg += "|Mã loại văn bản " + type + " không hợp lệ";

            Optional<SysCategoryDTO> orgOpt = sysCategories.stream()
                    .filter(o -> Objects.equals(o.getCode(), org))
                    .filter(o -> o.getType() == SysCategory.Type.DV_SO_HUU.value)
                    .findFirst();
            if (orgOpt.isEmpty()) msg += "|Mã đơn vị sở hữu " + org + " không hợp lệ";

            Optional<SysCategoryDTO> topicOpt = sysCategories.stream()
                    .filter(o -> Objects.equals(o.getCode(), topic))
                    .filter(o -> o.getType() == SysCategory.Type.TOPIC.value)
                    .findFirst();
            if (topicOpt.isEmpty()) msg += "|Mã topic " + topic + " không hợp lệ";

            String effectiveStatus = ExcelHelpers.getStringCellValue(row.getCell(10));
            Date issuedDate = row.getCell(8).getDateCellValue();
            Date effectiveDate = row.getCell(9).getDateCellValue();
            Date expireDate = null;
            if (Objects.equals(effectiveStatus, "0")) {
                expireDate = row.getCell(11).getDateCellValue();
            }
            if (issuedDate.after(today.getTime())) msg += "|Ngày ban hành không hợp lệ";
            if (effectiveDate.before(issuedDate)) msg += "|Ngày hiệu lực không hợp lệ";
            if (Objects.equals(effectiveStatus, "1") && expireDate != null && expireDate.before(effectiveDate))
                msg += "|Ngày hết hiệu lực không hợp lệ";

            Optional<DocInternal> docOpt = docInternalRepositoryJPA.findByCodeAndIsDelete(ExcelHelpers.getStringCellValue(row.getCell(0)), DocInternal.IsDelete.NO.value);
            if (docOpt.isPresent()) msg += "|Văn bản " + ExcelHelpers.getStringCellValue(row.getCell(0)) + " đã tồn tại";

            if (Objects.equals(ExcelHelpers.getStringCellValue(row.getCell(12)), "2") && (docOpt.isEmpty() || docOpt.get().getExpireDate().after(today.getTime()))) {
                msg += "|Văn bản " + ExcelHelpers.getStringCellValue(row.getCell(0)) + " không tồn tại";
            }

            if (!msg.equals("")) writeResult(row, "Failed", msg);
        } catch (Exception e) {
            log.error("handleImport: " + e);
            msg = "Dữ liệu đầu vào không hợp lệ";
        }
        if (!msg.equals("")) {
            writeResult(row, "Failed", msg);
            return false;
        }
        return true;
    }

    private String generateSqlWhere(DocInternalDTO dto) {
        String sqlWhere = " WHERE di.isDelete = 0";
        if (!StringUtils.isEmpty(dto.getCode())) sqlWhere += " AND LOWER(di.code) like :code";
        if (!StringUtils.isEmpty(dto.getTitle())) sqlWhere += " AND LOWER(di.title) like :title";
        if (dto.getType() != null) sqlWhere += " AND di.type = :type";
        if (dto.getOrgOwn() != null) sqlWhere += " AND di.orgOwn = :orgOwn";
        if (dto.getTopic() != null) sqlWhere += " AND di.topic = :topic";
        if (!StringUtils.isEmpty(dto.getEffectiveStatus())) sqlWhere += " AND di.effectiveStatus = :effectiveStatus";
        if (dto.getIssueDateFrom() != null) sqlWhere += " AND TRUNC(di.issuedDate) >= TRUNC(:issueDateFrom)";
        if (dto.getIssueDateTo() != null) sqlWhere += " AND TRUNC(di.issuedDate) <= TRUNC(:issueDateTo)";
        if (dto.getEffectiveDateFrom() != null) sqlWhere += " AND TRUNC(di.effectiveDate) >= TRUNC(:effectiveDateFrom)";
        if (dto.getEffectiveDateTo() != null) sqlWhere += " AND TRUNC(di.effectiveDate) <= TRUNC(:effectiveDateTo)";
        if (dto.getExpireDateFrom() != null) sqlWhere += " AND TRUNC(di.expireDate) >= TRUNC(:expireDateFrom)";
        if (dto.getExpireDateTo() != null) sqlWhere += " AND TRUNC(di.expireDate) <= TRUNC(:expireDateTo)";
        return sqlWhere;
    }

    private void setParameter(DocInternalDTO dto, TypedQuery<DocInternal> query, TypedQuery<Long> queryCount) {
        if (!StringUtils.isEmpty(dto.getCode())) {
            query.setParameter("code", "%" + dto.getCode().trim().toLowerCase());
            queryCount.setParameter("code", "%" + dto.getCode().trim().toLowerCase());
        }
        if (!StringUtils.isEmpty(dto.getTitle())) {
            query.setParameter("title", "%" + dto.getTitle().trim().toLowerCase());
            queryCount.setParameter("title", "%" + dto.getTitle().trim().toLowerCase());
        }
        if (dto.getType() != null) {
            query.setParameter("type", dto.getType());
            queryCount.setParameter("type", dto.getType());
        }
        if (dto.getOrgOwn() != null) {
            query.setParameter("orgOwn", dto.getOrgOwn());
            queryCount.setParameter("orgOwn", dto.getOrgOwn());
        }
        if (dto.getTopic() != null) {
            query.setParameter("topic", dto.getTopic());
            queryCount.setParameter("topic", dto.getTopic());
        }
        if (!StringUtils.isEmpty(dto.getEffectiveStatus())) {
            query.setParameter("effectiveStatus", dto.getEffectiveStatus());
            queryCount.setParameter("effectiveStatus", dto.getEffectiveStatus());
        }
        if (dto.getIssueDateFrom() != null) {
            query.setParameter("issueDateFrom", dto.getIssueDateFrom());
            queryCount.setParameter("issueDateFrom", dto.getIssueDateFrom());
        }
        if (dto.getIssueDateTo() != null) {
            query.setParameter("issueDateTo", dto.getIssueDateTo());
            queryCount.setParameter("issueDateTo", dto.getIssueDateTo());
        }
        if (dto.getEffectiveDateFrom() != null) {
            query.setParameter("effectiveDateFrom", dto.getEffectiveDateFrom());
            queryCount.setParameter("effectiveDateFrom", dto.getEffectiveDateFrom());
        }
        if (dto.getEffectiveDateTo() != null) {
            query.setParameter("effectiveDateTo", dto.getEffectiveDateTo());
            queryCount.setParameter("effectiveDateTo", dto.getEffectiveDateTo());
        }
        if (dto.getExpireDateFrom() != null) {
            query.setParameter("expireDateFrom", dto.getExpireDateFrom());
            queryCount.setParameter("expireDateFrom", dto.getExpireDateFrom());
        }
        if (dto.getExpireDateTo() != null) {
            query.setParameter("expireDateTo", dto.getExpireDateTo());
            queryCount.setParameter("expireDateTo", dto.getExpireDateTo());
        }
    }

    private void setParameterForExport(DocInternalDTO dto, TypedQuery<DocInternalDTO> query) {
        if (!StringUtils.isEmpty(dto.getCode())) query.setParameter("code", "%" + dto.getCode().trim().toLowerCase());
        if (!StringUtils.isEmpty(dto.getTitle())) query.setParameter("title", "%" + dto.getTitle().trim().toLowerCase());
        if (dto.getType() != null) query.setParameter("type", dto.getType());
        if (dto.getOrgOwn() != null) query.setParameter("orgOwn", dto.getOrgOwn());
        if (dto.getTopic() != null) query.setParameter("topic", dto.getTopic());
        if (!StringUtils.isEmpty(dto.getEffectiveStatus())) query.setParameter("effectiveStatus", dto.getEffectiveStatus());
        if (dto.getIssueDateFrom() != null) query.setParameter("issueDateFrom", dto.getIssueDateFrom());
        if (dto.getIssueDateTo() != null) query.setParameter("issueDateTo", dto.getIssueDateTo());
        if (dto.getEffectiveDateFrom() != null) query.setParameter("effectiveDateFrom", dto.getEffectiveDateFrom());
        if (dto.getEffectiveDateTo() != null) query.setParameter("effectiveDateTo", dto.getEffectiveDateTo());
        if (dto.getExpireDateFrom() != null) query.setParameter("expireDateFrom", dto.getExpireDateFrom());
        if (dto.getExpireDateTo() != null) query.setParameter("expireDateTo", dto.getExpireDateTo());
    }

    private boolean validateFileImport(Row row) {
        String msg = "";
        if (Objects.equals(ExcelHelpers.getStringCellValue(row.getCell(12)), "1") || Objects.equals(ExcelHelpers.getStringCellValue(row.getCell(12)), "2")) {
            if (Objects.equals(ExcelHelpers.getStringCellValue(row.getCell(0)), "")) msg += "|Mã văn bản không được để trống";
            if (Objects.requireNonNull(ExcelHelpers.getStringCellValue(row.getCell(0))).length() > 200) msg += "|Mã văn bản không được vượt quá 200 ký tự";
            if (Objects.equals(ExcelHelpers.getStringCellValue(row.getCell(1)), "")) msg += "|Tên văn bản không được để trống";
            if (Objects.requireNonNull(ExcelHelpers.getStringCellValue(row.getCell(1))).length() > 1000) msg += "|Tên văn bản không được vượt quá 1000 ký tự";
            if (Objects.equals(ExcelHelpers.getStringCellValue(row.getCell(3)), "")) msg += "|Mã loại văn bản không được để trống";
            if (Objects.equals(ExcelHelpers.getStringCellValue(row.getCell(5)), "")) msg += "|Mã đơn vị sở hữu không được để trống";
            if (Objects.equals(ExcelHelpers.getStringCellValue(row.getCell(7)), "")) msg += "|Mã topic không được để trống";
            if (Objects.equals(ExcelHelpers.getStringCellValue(row.getCell(8)), "")) msg += "|Ngày ban hành không được để trống";
            if (Objects.equals(ExcelHelpers.getStringCellValue(row.getCell(9)), "")) msg += "|Ngày hiệu lực không được để trống";
            if (Objects.equals(ExcelHelpers.getStringCellValue(row.getCell(10)), "")) msg += "|Tình trạng hiệu lực không được để trống";
            if (Objects.equals(ExcelHelpers.getStringCellValue(row.getCell(10)), "0") && Objects.equals(ExcelHelpers.getStringCellValue(row.getCell(11)), ""))
                msg += "|Ngày hết hiệu lực không được để trống";
            if (!DateUtil.isCellDateFormatted(row.getCell(8))) msg += "|Ngày ban hành không hợp lệ|Ngày ban hành format theo định dạng dd/mm/yyyy";
            if (!DateUtil.isCellDateFormatted(row.getCell(9))) msg += "|Ngày hiệu lực không hợp lệ|Ngày hiệu lực format theo định dạng dd/mm/yyyy";
            if (Objects.equals(ExcelHelpers.getStringCellValue(row.getCell(10)), "0") && !DateUtil.isCellDateFormatted(row.getCell(11))) msg += "|Ngày hết hiệu lực không hợp lệ|Ngày hết hiệu lực format theo định dạng dd/mm/yyyy";
        } else {
            if (Objects.equals(ExcelHelpers.getStringCellValue(row.getCell(12)), "")) msg += "|Hành động không được để trống";
            else msg += "Hành động không hợp lệ|Hành động chỉ chấp nhận 2 giá trị: 1: Thêm mới 2: Cập nhật";
        }
        if (!msg.isEmpty()) {
            writeResult(row, "Failed", msg);
            return false;
        }
        return true;
    }

    private void writeResult(Row row, String resultCode, String resultMgs) {
        Cell result = row.createCell(13);
        result.setCellType(CellType.STRING);
        result.setCellValue(resultCode);
        Cell description = row.createCell(14);
        description.setCellType(CellType.STRING);
        description.setCellValue(resultMgs);
    }
}
