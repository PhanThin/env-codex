package vn.com.viettel.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "PROJECT_ITEM")
public class ProjectItem {
    @Id
    @Column(name = "ITEM_ID", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    @JoinColumn(name = "PROJECT_ID", nullable = false)
    private Project project;

    @Size(max = 50)
    @NotNull
    @Column(name = "ITEM_CODE", nullable = false, length = 50)
    private String itemCode;

    @Size(max = 250)
    @NotNull
    @Column(name = "ITEM_NAME", nullable = false, length = 250)
    private String itemName;

    @Column(name = "UPDATED_BY")
    private Long updatedBy;

    @Column(name = "UPDATED_AT")
    private Instant updatedAt;

    @Column(name = "CREATED_BY")
    private Long createdBy;

    @Column(name = "CREATED_AT")
    private Instant createdAt;

    @Column(name = "IS_ACTIVE")
    private Boolean isActive;

    @Column(name = "IS_DELETED")
    private Boolean isDeleted;


}
