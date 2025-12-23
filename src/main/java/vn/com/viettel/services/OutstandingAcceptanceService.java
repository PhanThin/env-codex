
package vn.com.viettel.services;


import vn.com.viettel.dto.OutstandingAcceptanceDto;

/**
 * Service for OUTSTANDING_ACCEPTANCE.
 */
public interface OutstandingAcceptanceService {

    OutstandingAcceptanceDto create(Long outstandingId, OutstandingAcceptanceDto request);

    OutstandingAcceptanceDto update(Long outstandingId, Long acceptanceId, OutstandingAcceptanceDto request);

    OutstandingAcceptanceDto get(Long outstandingId);

    void delete(Long outstandingId, Long acceptanceId);
}
