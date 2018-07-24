package com.google.translate.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexMatches {

	/***
	 * 
	 * @param regex 正则表达式
	 * @param input 输入
	 * @return 正则表达式提取的结果
	 */
	public static String findFirstMatches(String regex, String input) {
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(input);
		
		
		String matchString = null;
		if (matcher.find()) {
			matchString = matcher.group(1);
		}
		
		
		return matchString;
	}

}
