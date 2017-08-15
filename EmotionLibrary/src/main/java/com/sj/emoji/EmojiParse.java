package com.sj.emoji;

public class EmojiParse
{
  public static String fromChar(char ch)
  {
    return Character.toString(ch);
  }
  public static String fromCodePoint(int codePoint) { return newString(codePoint); }

  public static final String newString(int codePoint) {
    if (Character.charCount(codePoint) == 1) {
      return String.valueOf(codePoint);
    }
    return new String(Character.toChars(codePoint));
  }
}