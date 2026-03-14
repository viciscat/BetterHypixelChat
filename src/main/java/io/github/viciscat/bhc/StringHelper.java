package io.github.viciscat.bhc;

import org.apache.commons.lang3.ArrayUtils;

public final class StringHelper {

    private StringHelper() {
        throw new AssertionError();
    }

    public static int findFirstDifferentChar(String string, int start, char c) {
        for (int i = start; i < string.length(); i++) {
            if (string.charAt(i) != c) return i;
        }
        return -1;
    }

    public static int findFirstDifferentChar(String string, int start, char... c) {
        for (int i = start; i < string.length(); i++) {
            if (!ArrayUtils.contains(c, string.charAt(i))) return i;
        }
        return -1;
    }

    public static int findLastDifferentChar(String string, int start, char c) {
        for (int i = start; i >= 0; i--) {
            if (string.charAt(i) != c) return i;
        }
        return -1;
    }

    public static int findLastDifferentChar(String string, int start, char... c) {
        for (int i = start; i >= 0; i--) {
            if (!ArrayUtils.contains(c, string.charAt(i))) return i;
        }
        return -1;
    }
}
