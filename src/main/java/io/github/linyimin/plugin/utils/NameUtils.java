package io.github.linyimin.plugin.utils;

/**
 * @author banzhe
 * @date 2022/12/06 10:38
 **/
public class NameUtils {
    /**
     * COPY FROM
     *
     * Convert singular name to plural form.
     *
     * @param name base name
     * @return plural name
     */
    public static String pluralize(String name) {
        // first check for already in plural form
        if (name.endsWith("List") || (name.endsWith("s") && !name.endsWith("ss"))) {
            return name;
        }

        // convert singular form to plural
        if (name.endsWith("y") && !name.endsWith("ay") && !name.endsWith("ey") && !name.endsWith("iy") &&
                !name.endsWith("oy") && !name.endsWith("uy")) {
            if (name.equalsIgnoreCase("any")) {
                return name;
            } else {
                return name.substring(0, name.length() - 1) + "ies";
            }
        } else if (name.endsWith("ss")) {
            return name + "es";
        } else {
            return name + 's';
        }
    }
}
