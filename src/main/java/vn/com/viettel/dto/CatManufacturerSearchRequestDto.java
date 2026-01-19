package vn.com.viettel.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CatManufacturerSearchRequestDto {

    private LocalDateTime createdAtFrom;
    private LocalDateTime createdAtTo;

    private String manufacturerCode;
    private String manufacturerName;
    private String country;

    private Boolean isActive;

    private Integer page;
    private Integer size;
    private String sortBy;
    private String sortDirection;
}
