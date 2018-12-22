package com.github.urlv.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class Util {
    public static String getFileExtension(String fileName) {
        int pos = fileName.lastIndexOf(".");
        return pos != -1 ? fileName.substring(pos + 1) : "";
    }

    public static String subString(String value, String till) {
        int pos = value.indexOf(till);
        return pos != -1 ? value.substring(0, pos) : value;
    }

    public static String decodeUrl(String url) {
        try {
            return URLDecoder.decode(url, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            return ""; // never happen
        }
    }
}
