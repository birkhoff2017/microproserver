package com.ycb.wpc.provider.utils;

import javax.net.ssl.X509TrustManager;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * 类名: MyX509TrustManager </br>
 * 包名： com.ycb.wpc.provider.menu.util
 * 描述: 证书信任管理器（用于https请求）  </br>
 * 开发人员：Bruce  </br>
 * 创建时间：  2017-10-7 </br>
 */
public class MyX509TrustManager implements X509TrustManager {

    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
    }

    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
    }

    public X509Certificate[] getAcceptedIssuers() {
        return null;
    }
}
