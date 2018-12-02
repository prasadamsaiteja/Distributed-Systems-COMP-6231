package server.instance4.pojo;

public enum UserType {

	STUDENT("S"), ADVISOR("A");

	private String value;

	UserType(final String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return value;
	}

	public static UserType fromString(String str) {
		for (UserType userType : UserType.values()) {
			if (userType.value.equalsIgnoreCase(str)) {
				return userType;
			}
		}
		throw new IllegalArgumentException(str);
	}

}
