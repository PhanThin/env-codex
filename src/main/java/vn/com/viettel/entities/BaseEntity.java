package vn.com.viettel.entities;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;

@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class BaseEntity {
    @CreatedDate
    @Column(name = "CREATED_DATE")
    Date createdDate;

    @CreatedBy
    @Column(name = "CREATED_USER")
    String createUser;

    @LastModifiedDate
    @Column(name = "MODIFIED_DATE")
    Date modifiedDate;

    @LastModifiedBy
    @Column(name = "MODIFIED_USER")
    String modifiedUser;
}
