package com.ronreynolds.util.string;

public class Latin1 {
    /**
     * Sanitize a string by replacing non-Latin-1 characters with their closest Latin-1 equivalents, and removing any remaining
     * non-Latin-1 characters.
     *
     * @param s the string to sanitize
     * @return the string <code>s</code> with no non-Latin-1 characters
     */
    public static String sanitize(String s) {
        return s == null ? s : s
                .replace('\u2011', '-')   // non-breaking hyphen → hyphen
                .replace('\u2013', '-')   // en dash
                .replace('\u2014', '-')   // em dash
                .replace('\u2018', '\'')  // left single quote
                .replace('\u2019', '\'')  // right single quote
                .replace('\u201C', '"')   // left double quote
                .replace('\u201D', '"')   // right double quote
                .replaceAll("[^\\x00-\\xFF]", "");
    }
}
