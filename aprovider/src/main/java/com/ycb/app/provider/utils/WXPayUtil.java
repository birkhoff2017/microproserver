package com.ycb.app.provider.utils;

import org.apache.commons.lang.math.RandomUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Map;
import java.util.Random;

/**
 * Created by zhuhui on 17-6-19.
 */
public class WXPayUtil {

    public static final Logger logger = LoggerFactory.getLogger(WXPayUtil.class);

    //生成随机字符串
    public static String getNonce_str() {
        String base = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 15; i++) {
            int number = random.nextInt(base.length());
            sb.append(base.charAt(number));
        }
        return sb.toString();
    }

    //map转xml 加上签名信息
    public static String map2Xml(Map<String, Object> map, String keyValue) throws UnsupportedEncodingException {
        StringBuffer sb = new StringBuffer();
        StringBuilder sb2 = new StringBuilder();
        sb2.append("<xml>");
        for (String key : map.keySet()) {
            sb.append(key);
            sb.append('=');
            sb.append(map.get(key));
            sb.append('&');
            // sb2是用来做请求的xml参数
            sb2.append("<" + key + ">");
            sb2.append("<![CDATA[" + map.get(key) + "]]>");
            // sb2.append(map.get(key));
            sb2.append("</" + key + ">");
        }
        sb.append("key=").append(keyValue);
        String sign = MD5.getMessageDigest(sb.toString().getBytes()).toUpperCase();
        sb2.append("<sign>");
        sb2.append(sign);
        sb2.append("</sign>");
        sb2.append("</xml>");
        return sb2.toString();
    }

    //解析微信返回return_code SUCCESS或FILE
    //根据微信返回resultXml再次生成签名
    public static String getSign(Map<String, Object> map, String keyValue) {
        StringBuffer sb = new StringBuffer();
        for (String key : map.keySet()) {
            sb.append(key);
            sb.append('=');
            sb.append(map.get(key));
            sb.append('&');
        }
        sb.append("key=").append(keyValue);
        System.out.println("第二次签名内容:" + sb);
        System.out.println("第二次签名SING:" + MD5.getMessageDigest(sb.toString().getBytes()).toUpperCase());
        return MD5.getMessageDigest(sb.toString().getBytes()).toUpperCase();
    }

    //解析微信返回return_code SUCCESS或FILE
    public static String getReturnCode(String resultXml) {
        String nonceStr;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        try {
            builder = dbf.newDocumentBuilder();
            InputStream inputStream = new ByteArrayInputStream(resultXml.getBytes());
            org.w3c.dom.Document doc = builder.parse(inputStream); //
            // 下面开始读取
            org.w3c.dom.Element root = doc.getDocumentElement(); // 获取根元素
            NodeList nl = root.getElementsByTagName("return_code");
            org.w3c.dom.Element el = (org.w3c.dom.Element) nl.item(0);
            org.w3c.dom.Node nd = el.getFirstChild();
            nonceStr = nd.getNodeValue();
            return nonceStr;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //解析微信返回return_msg
    public static String getReturn_msg(String resultXml) {
        String nonceStr;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        try {
            builder = dbf.newDocumentBuilder();
            InputStream inputStream = new ByteArrayInputStream(resultXml.getBytes());
            org.w3c.dom.Document doc = builder.parse(inputStream); //
            // 下面开始读取
            org.w3c.dom.Element root = doc.getDocumentElement(); // 获取根元素
            NodeList nl = root.getElementsByTagName("return_msg");
            org.w3c.dom.Element el = (org.w3c.dom.Element) nl.item(0);
            org.w3c.dom.Node nd = el.getFirstChild();
            nonceStr = nd.getNodeValue();
            return nonceStr;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //解析微信返回appid
    public static String getAppId(String resultXml) {
        String nonceStr;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        try {
            builder = dbf.newDocumentBuilder();
            InputStream inputStream = new ByteArrayInputStream(resultXml.getBytes());
            org.w3c.dom.Document doc = builder.parse(inputStream); //
            // 下面开始读取
            org.w3c.dom.Element root = doc.getDocumentElement(); // 获取根元素
            NodeList nl = root.getElementsByTagName("appid");
            org.w3c.dom.Element el = (org.w3c.dom.Element) nl.item(0);
            org.w3c.dom.Node nd = el.getFirstChild();
            nonceStr = nd.getNodeValue();
            return nonceStr;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //解析微信返回mch_id
    public static String getMchId(String resultXml) {
        String nonceStr;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        try {
            builder = dbf.newDocumentBuilder();
            InputStream inputStream = new ByteArrayInputStream(resultXml.getBytes());
            org.w3c.dom.Document doc = builder.parse(inputStream); //
            // 下面开始读取
            org.w3c.dom.Element root = doc.getDocumentElement(); // 获取根元素
            NodeList nl = root.getElementsByTagName("mch_id");
            org.w3c.dom.Element el = (org.w3c.dom.Element) nl.item(0);
            org.w3c.dom.Node nd = el.getFirstChild();
            nonceStr = nd.getNodeValue();
            return nonceStr;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //解析微信返回nonce_str
    public static String getNonceStr(String resultXml) {
        String nonceStr;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        try {
            builder = dbf.newDocumentBuilder();
            InputStream inputStream = new ByteArrayInputStream(resultXml.getBytes());
            org.w3c.dom.Document doc = builder.parse(inputStream); //
            // 下面开始读取
            org.w3c.dom.Element root = doc.getDocumentElement(); // 获取根元素
            NodeList nl = root.getElementsByTagName("nonce_str");
            org.w3c.dom.Element el = (org.w3c.dom.Element) nl.item(0);
            org.w3c.dom.Node nd = el.getFirstChild();
            nonceStr = nd.getNodeValue();
            return nonceStr;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @param resultXml 微信返回结果
     * @return
     * @author zhuhui
     * @date
     * @Description：解析微信返回prepay_id
     */
    public static String getPrepayId(String resultXml) {
        String nonceStr;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        try {
            builder = dbf.newDocumentBuilder();
            InputStream inputStream = new ByteArrayInputStream(resultXml.getBytes());
            org.w3c.dom.Document doc = builder.parse(inputStream); //
            // 下面开始读取
            org.w3c.dom.Element root = doc.getDocumentElement(); // 获取根元素
            NodeList nl = root.getElementsByTagName("prepay_id");
            org.w3c.dom.Element el = (org.w3c.dom.Element) nl.item(0);
            org.w3c.dom.Node nd = el.getFirstChild();
            nonceStr = nd.getNodeValue();
            return nonceStr;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @param return_code 返回编码
     * @param return_msg  返回信息
     * @return
     * @author zhuhui
     * @date
     * @Description：返回给微信的参数
     */
    public static String setXML(String return_code, String return_msg) {
        return "<xml><return_code><![CDATA[" + return_code
                + "]]></return_code><return_msg><![CDATA[" + return_msg
                + "]]></return_msg></xml>";
    }

    /**
     * 检验API返回的数据里面的签名是否合法，避免数据在传输的过程中被第三方篡改
     *
     * @param responseStr API返回的XML数据字符串
     * @param key         商户Key
     * @return API签名是否合法
     */
    public static boolean checkIsSignValidFromResponseString(String responseStr, String key) {
        try {
            Map<String, Object> map = XmlUtil.doXMLParse(responseStr);
            String signFromAPIResponse = map.get("sign").toString();
            if ("".equals(signFromAPIResponse) || signFromAPIResponse == null) {
                logger.debug("API返回的数据签名数据不存在，有可能被第三方篡改!!!");
                return false;
            }
            logger.debug("服务器回包里面的签名是:" + signFromAPIResponse);
            //清掉返回数据对象里面的Sign数据（不能把这个数据也加进去进行签名），然后用签名算法进行签名
            map.remove("sign");
            //将API返回的数据根据用签名算法进行计算新的签名，用来跟API返回的签名进行比较
            String signForAPIResponse = getSign(map, key);
            if (!signForAPIResponse.equals(signFromAPIResponse)) {
                //签名验不过，表示这个API返回的数据有可能已经被篡改了
                logger.debug("API返回的数据签名验证不通过，有可能被第三方篡改!!!");
                return false;
            }
            logger.debug("恭喜，API返回的数据签名验证通过!!!");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 订单号生成
     *
     * @return
     */
    public static String createOrderId() {
        String yyyyMMdd = DateFormatUtils.format(new Date(), "yyyyMMdd");
        String hhmmss = DateFormatUtils.format(new Date(), "HHmmss");
        int randomNum = RandomUtils.nextInt(99999);
        return "MCS-" + yyyyMMdd + "-" + hhmmss + "-" + String.format("%05d", randomNum);
    }
}
