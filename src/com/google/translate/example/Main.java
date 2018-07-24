package com.google.translate.example;

import com.google.translate.Translate;

public class Main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String result1 = Translate.translate("傻逼");
		String result2 = Translate.translate("大傻逼", "zh-CN", "ja");
		
		System.out.println(result1);
		System.out.println(result2);
	}

}
