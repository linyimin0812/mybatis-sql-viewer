package io.github.linyimin.plugin.pojo2json;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author banzhe
 * @date 2022/11/21 20:53
 **/
public class POJO2JSONParserUtils {
    public static String psiTextToString(String psiText) {
        return psiText.replace("\"", "");
    }

    public static List<String> arrayTextToList(String text) {

        text = StringUtils.deleteWhitespace(text);

        boolean array = text.length() > 2 &&
                ((text.startsWith("{") && text.endsWith("}")) ||   // Java
                        (text.startsWith("(") && text.endsWith(")"))); // Kotlin
        if (array) {

            return Arrays.stream(text.substring(1, text.length() - 1)
                            .replace("\"", "")
                            .split(","))
                    .collect(Collectors.toList());

        } else if (text.matches("^\"\\w+\"$")) {
            return Collections.singletonList(text.replace("\"", ""));
        }

        return Collections.emptyList();
    }

    public static List<String> docTextToList(String tags, String text) {

        if (!text.contains(tags)) {
            return Collections.emptyList();
        }

        int start = text.indexOf(tags) + tags.length();
        int end = start;
        while (text.charAt(end) != '\n') {
            end++;
        }
        if (start == end) {
            return Collections.emptyList();
        }

        return Arrays.stream(StringUtils.deleteWhitespace(text.substring(start, end + 1)).split(","))
                .collect(Collectors.toList());
    }
}
