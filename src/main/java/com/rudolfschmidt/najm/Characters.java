package com.rudolfschmidt.najm;

public class Characters {
    public static boolean isFirstLetterNotLowerCase(String str) {
        return !Character.isLowerCase(str.charAt(0));
    }
    public static String makeFirstLetterLowerCase(String str) {
        return Character.toLowerCase(str.charAt(0)) + str.substring(1);
    }
}
