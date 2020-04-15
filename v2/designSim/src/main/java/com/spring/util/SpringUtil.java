package com.spring.util;

public class SpringUtil {
    public static String changeName(String className){
        char[] chars = className.toCharArray();
        if(chars[0]<='Z'&& chars[0] >= 'A'){
            chars[0] = (char) (chars[0] + 32);
        }
        return String.copyValueOf(chars);
    }
}
