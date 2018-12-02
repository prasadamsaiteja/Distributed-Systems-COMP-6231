package server.instance4.pojo;

import java.time.LocalDate;

public enum Semester {
	FALL, WINTER, SUMMER;

	public static Semester getCurrentSemester(LocalDate d) {
		Semester semester = null;
		if (d.getMonthValue() <= 4) {
			semester = WINTER;
		} else if (d.getMonthValue() >= 9) {
			semester = FALL;
		} else {
			semester = SUMMER;
		}
		return semester;
	}

	public static boolean isValidSemester(final String sem) {
		for (Semester s : Semester.values()) {
			if (s.toString().equalsIgnoreCase(sem))
				return true;
		}
		return false;
	}

}
