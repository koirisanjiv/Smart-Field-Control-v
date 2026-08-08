package com.qaverse.smart.FieldAccessControl.Configuration;

public enum UserType {

	SUPER_ADMIN("superadmin", "super_admin", "super-admin"),

	ADMIN("admin"),

	ADMIN_SUB_USER("admin_sub_user", "admin-sub-user", "adminsubuser"),

	RESELLER("reseller"),

	RESELLER_SUB_USER("reseller_sub_user", "reseller-sub-user", "resellersubuser"),

	COMPANY("company"),

	COMPANY_SUB_USER("company_sub_user", "company-sub-user", "companysubuser");

	private final String[] aliases;

	UserType(String... aliases) {
		this.aliases = aliases;
	}

	public boolean matches(String value) {

		String input = normalize(value);

		for (String alias : aliases) {

			if (normalize(alias).equals(input)) {
				return true;
			}
		}

		return false;
	}

	private static String normalize(String value) {

		return value.trim().toLowerCase().replaceAll("[\\s_-]+", "");
	}

}