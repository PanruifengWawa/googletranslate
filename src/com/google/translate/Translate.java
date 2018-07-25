package com.google.translate;

import java.util.ArrayList;
import java.util.List;
import java.net.URLEncoder;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;


import com.google.translate.util.HttpUtil;
import com.google.translate.util.RegexMatches;

public class Translate {
	private static String translateUrl = "https://translate.google.cn";
	private static String translateAPI = translateUrl
			+ "/translate_a/single?client=t&hl=zh-CN&dt=at&dt=bd&dt=ex&dt=ld&dt=md&dt=qca&dt=rw&dt=rm&dt=ss&dt=t&ie=UTF-8&oe=UTF-8&source=btn&ssel=3&tsel=3&kc=0";
	private static String defaultSL = "zh-CN"; // 默认翻译源
	private static String defaultHL = "en"; // 默认翻译目标

	private static String tkk = null; //谷歌翻译页面中的验证值

	private static String regex = "TKK=(.*);VERSION_LABEL"; //正则表达式，用于获取翻译页面中的tkk
	
	
	public static String translate(String text) { 
		return translate(text, defaultSL, defaultHL);
	}
	
	public static String translate(String text, String sl, String hl) {
		setTKK();
		
		String tk = getTK(text);
		
		try {
			text = URLEncoder.encode(text,"utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		String apiUrl = translateAPI + "&sl=" + sl + "&hl=" + hl + "&q=" + text + "&tk=" + tk;
		String translatedText = HttpUtil.sendGet(apiUrl);
		return translatedText;
	}

	
	 
	/***
	 * 	谷歌翻译tk生成函数
	 * 	@param text 需要被翻译的文本
	 * 
	 * 	谷歌源码如下
	 *	var tk =  function (a,TKK) {
     *		for (var e = TKK.split("."), h = Number(e[0]) || 0, g = [], d = 0, f = 0; f < a.length; f++) {
     *			var c = a.charCodeAt(f);
     *   		128 > c ? g[d++] = c : (2048 > c ? g[d++] = c >> 6 | 192 : (55296 == (c & 64512) && f + 1 < a.length && 56320 == (a.charCodeAt(f + 1) & 64512) ? (c = 65536 + ((c & 1023) << 10) + (a.charCodeAt(++f) & 1023), g[d++] = c >> 18 | 240, g[d++] = c >> 12 & 63 | 128) : g[d++] = c >> 12 | 224, g[d++] = c >> 6 & 63 | 128), g[d++] = c & 63 | 128)
     * 		}
     * 		a = h;
     * 		for (d = 0; d < g.length; d++) a += g[d], a = b(a, "+-a^+6");
     * 		a = b(a, "+-3^+b+-f");
     * 		a ^= Number(e[1]) || 0;
     * 		0 > a && (a = (a & 2147483647) + 2147483648);
     * 		a %= 1E6;
     * 		return a.toString() + "." + (a ^ h)
	 * 	}
	 */
	private static String getTK(String text) {
		List<Integer> g = new ArrayList<>();
		String[] e = tkk.split("\\.");
		int h = 0;
		try {
			h = Integer.parseInt(e[0]);
		} catch (Exception e2) {
			e2.printStackTrace();
		}
		for (int f = 0; f < text.length(); f++) {
			
            int c = text.charAt(f);

            if(128 > c) {
            	g.add(c);
            } else {
            	if(2048 > c) {
            		g.add(c >> 6 | 192);
            	} else {
            		if(55296 == (c & 64512) && f + 1 < text.length() && 56320 == (text.charAt(f + 1) & 64512)) {
            			c = 65536 + ((c & 1023) << 10) + (text.charAt(++f) & 1023);
            			g.add(c >> 18 | 240);
            			g.add(c >> 12 & 63 | 128);
           			} else {
           				g.add(c >> 12 | 224);
           				g.add(c >> 6 & 63 | 128);
           						
           			}
           		}
            	g.add(c & 63 | 128);
            }
		}
		long a = h;
        for (Integer xInteger : g) {
        	a += xInteger;
        	a = tkUtil(a, "+-a^+6");
        }
        a = tkUtil(a, "+-3^+b+-f");
        try {
        	a ^= Integer.parseInt(e[1]);
		} catch (Exception e2) {
			e2.printStackTrace();
			a ^= 0;
		}
        if (0 > a) {
        	a = (int) ((a & 2147483647) + Long.parseLong("2147483648"));
		}

        a %= 1E6;
        return a + "." + (a ^ h);
		
	}

	/***
	 * 谷歌翻译tk生成辅助函数，源码如下
	 * var b = function (a, b) {
     * 	for (var d = 0; d < b.length - 2; d += 3) {
     * 		var c = b.charAt(d + 2),
     *      c = "a" <= c ? c.charCodeAt(0) - 87 : Number(c),
     *      c = "+" == b.charAt(d + 1) ? a >>> c : a << c;
     *    	a = "+" == b.charAt(d) ? a + c & 4294967295 : a ^ c
     * 	}
     * 	return a
	 * }
	 */
	private static long tkUtil(long a, String b) {
		for (int d = 0; d < b.length() - 2; d += 3) {
			char c = b.charAt(d + 2);
			int c1 = 'a' <= c ? (char) (c - 87) : Integer.parseInt(String.valueOf(c));
			long c2 = '+' == b.charAt(d + 1) ? a >>> c1 : a << c1;
			a = '+' == b.charAt(d) ? a + c2 & Long.valueOf("4294967295") : a ^ c2;
		}
		return a;
	}
	
	/***
	 * 获取翻译页面，并且提取界面中的tkk
	 */
	private static void setTKK() {
		if (tkk == null) { //本脚本中，只在第一次使用的时候刷新。在不同的场景中，请设置自己的刷新频率，避免被发现
			String translatePage = HttpUtil.sendGet(translateUrl);
			tkk = RegexMatches.findFirstMatches(regex, translatePage);
			ScriptEngineManager manager = new ScriptEngineManager();
			ScriptEngine se = manager.getEngineByName("js");
			try {
				tkk = (String) se.eval(tkk);
			} catch (ScriptException e) {
				e.printStackTrace();
			}
		}
	}
}
