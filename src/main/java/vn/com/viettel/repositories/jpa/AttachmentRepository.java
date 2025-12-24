package vn.com.viettel.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.viettel.entities.Attachment;

import java.util.List;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
    List<Attachment> findAllByReferenceIdAndReferenceTypeAndIsDeletedFalse(Long referenceId, String referenceType);

    List<Attachment> findAllByReferenceIdInAndReferenceTypeAndIsDeletedFalse(List<Long> referenceIds, String referenceType);

    List<Attachment> findAllByIdInAndIsDeletedFalse(List<Long> ids);

    List<Attachment> findAllByReferenceIdAndReferenceTypeInAndIsDeletedFalse(Long referenceId, List<String> referenceType);
}
