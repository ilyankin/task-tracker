package utils;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Web {

    public static List<Map.Entry<String, String>> getQueryParameters(URI uri) {
        String query = uri.getQuery();
        if (query == null || query.isBlank()) return Collections.emptyList();
        return Pattern.compile("&")
                .splitAsStream(query.trim())
                .map(s -> Arrays.copyOf(s.split("=", 2), 2))
                .map(o -> Map.entry(decode(o[0]), decode(o[1])))
                .collect(Collectors.toList());
    }

    private static String decode(final String encoded) {
        return Optional.ofNullable(encoded)
                .map(e -> URLDecoder.decode(e, StandardCharsets.UTF_8))
                .orElse(null);
    }
}
