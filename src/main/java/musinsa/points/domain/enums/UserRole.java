package musinsa.points.domain.enums;

public enum UserRole {
    CUSTOMER(Authority.CUSTOMER),  // 사용자 권한
    MANAGER(Authority.MANAGER);

    private final String authority;

    UserRole(String authority) {
        this.authority = authority;
    }

    public String getAuthority() {
        return this.authority;
    }

    public static class Authority {
        public static final String CUSTOMER = "ROLE_CUSTOMER";
        public static final String MANAGER = "ROLE_MANAGER";

    }
}
