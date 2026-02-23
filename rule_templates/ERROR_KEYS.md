# ERROR KEYS – PADT (messages_vi/en)

> Danh sách message keys tối thiểu cho module PADT.  
> Quy ước:  
> - 404: not found  
> - 403: forbidden  
> - 400: invalid input / validation / invalid status  
> - 409: duplicate / conflict  
> - 501: catalog not integrated / missing integration

---

## 1) Core Plan Errors

- `padt.plan.notFound` (404)
- `padt.plan.forbidden` (403)
- `padt.plan.invalidStatus` (400)
- `padt.plan.invalidTransition` (400)
- `padt.plan.comment.tooLong` (400)

---

## 2) Workflow Action Errors

- `padt.plan.reject.commentRequired` (400)

---

## 3) Personnel Validation Errors

- `padt.personnel.requireOneSupervisor` (400)
- `padt.personnel.requireAtLeastOneSurveyor` (400)
- `padt.personnel.duplicateSurveyor` (409)

---

## 4) Equipment Validation Errors

- `padt.equipment.invalidId` (400)
- `padt.equipment.duplicate` (409)

---

## 5) Task Validation Errors

- `padt.task.invalidDateRange` (400)
- `padt.task.missingLocations` (400)
- `padt.task.missingPersonnel` (400)

---

## 6) Task Personnel Membership Errors

- `padt.taskPersonnel.notSurveyorOfPlan` (400)

---

## 7) Batch Action Errors

- `padt.batch.invalidItems` (400)

---

## 8) Catalog / Integration Errors

- `padt.plan.catalog.notIntegrated` (501)

---

## 9) Mapping gợi ý Status → Key (Reference)

| Tình huống | HTTP | Key |
|---|---:|---|
| planId không tồn tại | 404 | padt.plan.notFound |
| user không có quyền | 403 | padt.plan.forbidden |
| status không đúng để thao tác | 400 | padt.plan.invalidStatus |
| transition sai state machine | 400 | padt.plan.invalidTransition |
| reject thiếu comment | 400 | padt.plan.reject.commentRequired |
| comment > 100 | 400 | padt.plan.comment.tooLong |
| duplicate surveyor/equipment | 409 | padt.personnel.duplicateSurveyor / padt.equipment.duplicate |
| equipmentId không hợp lệ | 400 | padt.equipment.invalidId |
| catalog chưa tích hợp | 501 | padt.plan.catalog.notIntegrated |

---

## 10) Sample messages (VI/EN) – Skeleton

### messages_vi.properties (gợi ý)
- `padt.plan.notFound=Không tìm thấy kế hoạch khảo sát.`
- `padt.plan.forbidden=Bạn không có quyền thực hiện thao tác này.`
- `padt.plan.invalidStatus=Trạng thái kế hoạch không hợp lệ cho thao tác hiện tại.`
- `padt.plan.invalidTransition=Chuyển trạng thái không hợp lệ.`
- `padt.plan.reject.commentRequired=Lý do từ chối là bắt buộc.`
- `padt.plan.comment.tooLong=Nội dung ghi chú không được vượt quá {0} ký tự.`
- `padt.personnel.requireOneSupervisor=Kế hoạch phải có đúng 1 giám sát (SUPERVISOR).`
- `padt.personnel.requireAtLeastOneSurveyor=Kế hoạch phải có ít nhất 1 khảo sát viên (SURVEYOR).`
- `padt.personnel.duplicateSurveyor=Khảo sát viên bị trùng trong danh sách.`
- `padt.equipment.invalidId=Thiết bị không hợp lệ hoặc không tồn tại.`
- `padt.equipment.duplicate=Thiết bị bị trùng trong kế hoạch.`
- `padt.task.invalidDateRange=Khoảng thời gian thực hiện task không hợp lệ.`
- `padt.task.missingLocations=Task phải có ít nhất 1 địa điểm.`
- `padt.task.missingPersonnel=Task phải có ít nhất 1 nhân sự.`
- `padt.taskPersonnel.notSurveyorOfPlan=Nhân sự trong task phải thuộc danh sách SURVEYOR của kế hoạch.`
- `padt.batch.invalidItems=Danh sách phê duyệt/từ chối batch không hợp lệ.`
- `padt.plan.catalog.notIntegrated=Chức năng catalog chưa được tích hợp.`

### messages_en.properties (gợi ý)
- `padt.plan.notFound=Survey plan not found.`
- `padt.plan.forbidden=You are not allowed to perform this action.`
- `padt.plan.invalidStatus=Invalid plan status for this action.`
- `padt.plan.invalidTransition=Invalid workflow transition.`
- `padt.plan.reject.commentRequired=Reject comment is required.`
- `padt.plan.comment.tooLong=Comment must not exceed {0} characters.`
- `padt.personnel.requireOneSupervisor=Plan must have exactly one SUPERVISOR.`
- `padt.personnel.requireAtLeastOneSurveyor=Plan must have at least one SURVEYOR.`
- `padt.personnel.duplicateSurveyor=Duplicate surveyor found in the list.`
- `padt.equipment.invalidId=Invalid or non-existing equipment.`
- `padt.equipment.duplicate=Duplicate equipment found in the plan.`
- `padt.task.invalidDateRange=Invalid task date range.`
- `padt.task.missingLocations=Task must have at least one location.`
- `padt.task.missingPersonnel=Task must have at least one personnel.`
- `padt.taskPersonnel.notSurveyorOfPlan=Task personnel must be a SURVEYOR of the plan.`
- `padt.batch.invalidItems=Invalid batch action items.`
- `padt.plan.catalog.notIntegrated=Catalog integration is not available.`

