package com.pax.pay.app;

public class MacroDefine {

    /**
     * 主密钥索引模式
     * 
     * 默认(0x01);<br>
     * a. 0x01.内置:index * 2 + 1,外置:index * 2 + 1;<br>
     * b. 0x02.内置:index * 2 + 1,外置:index * 2;<br>
     */
    public final static byte MAINKEY_INDEX_MODE = 0x01;
}
