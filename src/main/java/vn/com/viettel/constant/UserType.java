package vn.com.viettel.constant;

public enum UserType {
    IMIS(0), // Tài khoản noi bo, không check chinh sach mat khau
    INTERNAL(1); // Tài khoản khách, check chinh sach mat khau

    private final Integer value;
    UserType(Integer value) { this.value = value; }
    public Integer getValue() { return value; }
}