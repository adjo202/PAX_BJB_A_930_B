package com.pax.pay.trans.pack;

import com.pax.abl.core.ipacker.PackListener;

/**
 * IC卡公钥下载状态上送
 * 
 * @author Steven.W
 * 
 */
public class PackBlackDownload extends PackBinDownload {

    public static final String TAG = "PackBlackDownload";

    public PackBlackDownload(PackListener listener) {
        super(listener);

    }

}
