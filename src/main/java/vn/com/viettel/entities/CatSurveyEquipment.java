package vn.com.viettel.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.type.YesNoConverter;

import java.time.LocalDateTime;
@Getter
@Setter
@Entity
@Table(name = "CAT_SURVEY_EQUIPMENT")
public class CatSurveyEquipment {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_CAT_SURVEY_EQUIPMENT")
    @SequenceGenerator(name = "SEQ_CAT_SURVEY_EQUIPMENT", sequenceName = "SEQ_CAT_SURVEY_EQUIPMENT", allocationSize = 1)
    @Column(name = "EQUIPMENT_ID", nullable = false)
    private Long equipmentId;

    @Column(name = "EQUIPMENT_CODE", nullable = false, length = 1000)
    private String equipmentCode;

    @Column(name = "EQUIPMENT_NAME", nullable = false, length = 1000)
    private String equipmentName;

    @Column(name = "MODEL_CODE", nullable = false, length = 1000)
    private String modelCode;

    @Column(name = "MANUFACTURER_ID")
    private Long manufacturerId;

    @Column(name = "MANUFACTURER_NAME", length = 1000)
    private String manufacturerName;

    @Column(name = "MANUFACTURE_YEAR")
    private Integer manufactureYear;

    @Column(name = "UOM_ID")
    private Long uomId;

    @Column(name = "UOM_NAME", length = 200)
    private String uomName;

    @Column(name = "MANAGE_UNIT_ID", nullable = false)
    private Long manageUnitId;

    @Column(name = "MANAGE_UNIT_NAME", length = 1000)
    private String manageUnitName;

    @Column(name = "NOTE", length = 2000)
    private String note;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "CREATED_BY")
    private Long createdBy;

    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    @Column(name = "UPDATED_BY")
    private Long updatedBy;

    @Column(name = "IS_DELETED", nullable = false, length = 1)
    @Convert(converter = YesNoConverter.class)
    private Boolean isDeleted;

    @Column(name = "IS_ACTIVE", nullable = false, length = 1)
    @Convert(converter = YesNoConverter.class)
    private Boolean isActive;
}
