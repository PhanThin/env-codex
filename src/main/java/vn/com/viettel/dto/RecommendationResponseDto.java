package vn.com.viettel.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for {@link vn.com.viettel.entities.RecommendationResponse}
 */
@Getter
@Setter
@NoArgsConstructor
public class RecommendationResponseDto implements Serializable {
    Long id;
    Long recommendationId;
    UserDto respondedByUser;
    LocalDateTime respondedAt;
    @Size(max = 2000)
    String responseContent;
    List<AttachmentDto> attachments;
    UserDto redirectToUser;
    Boolean isRedirect;
}
