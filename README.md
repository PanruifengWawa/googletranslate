## 说明
通过调用谷歌翻译界面的接口，实现翻译接口的匿名调用。
python版本的已经有人写好了
``` python
pip install googletrans


#-*- coding:utf-8 -*-
from googletrans import Translator
import sys

reload(sys)
sys.setdefaultencoding( "utf-8" )

translator = Translator()
print translator.translate('今天天气不错').text
print translator.translate('今天天气不错', dest='ja').text
print translator.translate('今天天气不错', dest='ko').text
```
但是我没有发现java版本的，就自己实现了一下

## 原理
首先说明一下，谷歌翻译的界面是如何调用它自己的接口的

 - 1. 访问https://translate.google.cn/ ，页面中包含一个叫做TKK的参数，如图
![](/images/TKK.jpg)
 - 2. 这个TKK与需要被翻译的文本生成一张ticket(tk)，生成方式如下

 ``` javascript
 //辅助函数
 var b = function (a, b) {
    for (var d = 0; d < b.length - 2; d += 3) {
        var c = b.charAt(d + 2),
            c = "a" <= c ? c.charCodeAt(0) - 87 : Number(c),
            c = "+" == b.charAt(d + 1) ? a >>> c : a << c;
        a = "+" == b.charAt(d) ? a + c & 4294967295 : a ^ c
    }
    return a
}
//a就是文本
var tk =  function (a,TKK) {
    for (var e = TKK.split("."), h = Number(e[0]) || 0, g = [], d = 0, f = 0; f < a.length; f++) {
        var c = a.charCodeAt(f);
        128 > c ? g[d++] = c : (2048 > c ? g[d++] = c >> 6 | 192 : (55296 == (c & 64512) && f + 1 < a.length && 56320 == (a.charCodeAt(f + 1) & 64512) ? (c = 65536 + ((c & 1023) << 10) + (a.charCodeAt(++f) & 1023), g[d++] = c >> 18 | 240, g[d++] = c >> 12 & 63 | 128) : g[d++] = c >> 12 | 224, g[d++] = c >> 6 & 63 | 128), g[d++] = c & 63 | 128)
    }
    a = h;
    for (d = 0; d < g.length; d++) a += g[d], a = b(a, "+-a^+6");
    a = b(a, "+-3^+b+-f");
    a ^= Number(e[1]) || 0;
    0 > a && (a = (a & 2147483647) + 2147483648);
    a %= 1E6;
    return a.toString() + "." + (a ^ h)
}
 ```
 - 3. 把tk和文本当做参数请求api，并返回结果

 ``` javascript
//接口，tk为ticket，q为文本，sl为翻译源语言，hl为翻译目标语言，其他参数我没比较
https://translate.google.cn/translate_a/single?client=t&sl=zh-CN&tl=zh-CN&hl=zh-CN&dt=at&dt=bd&dt=ex&dt=ld&dt=md&dt=qca&dt=rw&dt=rm&dt=ss&dt=t&ie=UTF-8&oe=UTF-8&ssel=0&tsel=3&kc=0&tk=327521.164273&q=aaa

//返回结果
[
    [
        ["Stupid", "傻逼", null, null, 3],
        [null, null, null, "Shǎbī"]
    ], null, "zh-CN", null, null, [
        ["傻逼", null, [
                ["Stupid", 0, true, false],
                ["Sucker", 0, true, false]
            ],
            [
                [0, 2]
            ], "傻逼", 0, 0
        ]
    ], 0, null, [
        ["zh-CN"], null, [0],
        ["zh-CN"]
    ]
]
 ```

## 实现
按照原理，进行实现

 - 1. 通过java http client请求https://translate.google.cn/ 源码，用正则表达式提取其中的TKK那部分的代码，用java执行这部分的js代码，并获取真实的TKK的值
 - 2. 用java实现ticket函数

``` java
private static long tkUtil(long a, String b) {
    for (int d = 0; d < b.length() - 2; d += 3) {
        char c = b.charAt(d + 2);
        int c1 = 'a' <= c ? (char) (c - 87) : Integer.parseInt(String.valueOf(c));
        long c2 = '+' == b.charAt(d + 1) ? a >>> c1 : a << c1;
        a = '+' == b.charAt(d) ? a + c2 & Long.valueOf("4294967295") : a ^ c2;
    }
    return a;
}

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
```

 - 3. 拼装api接口，并调用。

 - 4. 本项目中提供源码的[example](https://github.com/PanruifengWawa/googletranslate/blob/master/src/com/google/translate/example/Main.java)如下
 
``` java
String result1 = Translate.translate("傻逼");
String result2 = Translate.translate("大傻逼", "zh-CN", "ja");
System.out.println(result1);
System.out.println(result2);

//result1
[
    [
        ["Stupid", "傻逼", null, null, 3],
        [null, null, null, "Shǎbī"]
    ], null, "zh-CN", null, null, [
        ["傻逼", null, [
                ["Stupid", 0, true, false],
                ["Sucker", 0, true, false]
            ],
            [
                [0, 2]
            ], "傻逼", 0, 0
        ]
    ], 0, null, [
        ["zh-CN"], null, [0],
        ["zh-CN"]
    ]
]
//result2
[
    [
        ["巨大なばか", "大傻逼", null, null, 3],
        [null, null, "Kyodaina baka", "Dà shǎbī"]
    ], null, "zh-CN", null, null, [
        ["大傻逼", null, [
                ["巨大なばか", 0, true, false],
                ["ビッグ吸盤", 0, true, false]
            ],
            [
                [0, 3]
            ], "大傻逼", 0, 0
        ]
    ], 1, null, [
        ["zh-CN"], null, [1],
        ["zh-CN"]
    ]
]
```





