package com.ycb.wpc.provider.utils;

import com.ycb.wpc.provider.cache.RedisService;
import com.ycb.wpc.provider.constant.GlobalConfig;
import com.ycb.wpc.provider.constant.MessageReply;
import com.ycb.wpc.provider.vo.Menu;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.net.ConnectException;
import java.net.URL;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Map;


/**
 * 类名: WeixinUtil </br>
 * 包名： com.ycb.wpc.provider.menu.util
 * 描述: 公众平台通用接口工具类 </br>
 * 开发人员： Bruce  </br>
 * 创建时间：  2017-10-7 </br>
 */
public class WeixinUtil {

    private static Logger logger = LoggerFactory.getLogger(WeixinUtil.class);

    // 菜单创建（POST） 限100（次/天）
    public static String menuCreateUrl = "https://api.weixin.qq.com/cgi-bin/menu/create?access_token=ACCESS_TOKEN";

    //个性化菜单创建
    public static String personalityMenuCreateUrl = "https://api.weixin.qq.com/cgi-bin/menu/addconditional?access_token=ACCESS_TOKEN";

    /**
     * 创建菜单
     *
     * @param menu        菜单实例
     * @param accessToken 有效的access_token
     * @return 0表示成功，其他值表示失败
     */
    public static int createMenu(Menu menu, String accessToken) {
        int result = 0;
        // 拼装创建菜单的url
        String url = menuCreateUrl.replace("ACCESS_TOKEN", accessToken);
        // 将菜单对象转换成json字符串
        String jsonMenu = JSONObject.fromObject(menu).toString();
        // 调用接口创建菜单
        JSONObject jsonObject = httpRequest(url, "POST", jsonMenu);
        if (null != jsonObject) {
            if (0 != jsonObject.getInt("errcode")) {
                result = jsonObject.getInt("errcode");
                logger.error("创建菜单失败 errcode:{} errmsg:{}", jsonObject.getInt("errcode"), jsonObject.getString("errmsg"));
            }
        }

        return result;
    }


    /**
     * 描述:  发起https请求并获取结果
     *
     * @param requestUrl    请求地址
     * @param requestMethod 请求方式（GET、POST）
     * @param outputStr     提交的数据
     * @return JSONObject(通过JSONObject.get(key)的方式获取json对象的属性值)
     */
    public static JSONObject httpRequest(String requestUrl, String requestMethod, String outputStr) {
        JSONObject jsonObject = null;
        StringBuffer buffer = new StringBuffer();
        try {
            // 创建SSLContext对象，并使用我们指定的信任管理器初始化
            TrustManager[] tm = {new MyX509TrustManager()};
            SSLContext sslContext = SSLContext.getInstance("SSL", "SunJSSE");
            sslContext.init(null, tm, new java.security.SecureRandom());
            // 从上述SSLContext对象中得到SSLSocketFactory对象
            SSLSocketFactory ssf = sslContext.getSocketFactory();

            URL url = new URL(requestUrl);
            HttpsURLConnection httpUrlConn = (HttpsURLConnection) url.openConnection();
            httpUrlConn.setSSLSocketFactory(ssf);

            httpUrlConn.setDoOutput(true);
            httpUrlConn.setDoInput(true);
            httpUrlConn.setUseCaches(false);

            // 设置请求方式（GET/POST）
            httpUrlConn.setRequestMethod(requestMethod);

            if ("GET".equalsIgnoreCase(requestMethod))
                httpUrlConn.connect();

            // 当有数据需要提交时
            if (null != outputStr) {
                OutputStream outputStream = httpUrlConn.getOutputStream();
                // 注意编码格式，防止中文乱码
                outputStream.write(outputStr.getBytes("UTF-8"));
                outputStream.close();
            }

            // 将返回的输入流转换成字符串
            InputStream inputStream = httpUrlConn.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String str = null;
            while ((str = bufferedReader.readLine()) != null) {
                buffer.append(str);
            }
            bufferedReader.close();
            inputStreamReader.close();
            // 释放资源
            inputStream.close();
            inputStream = null;
            httpUrlConn.disconnect();
            jsonObject = JSONObject.fromObject(buffer.toString());
        } catch (ConnectException ce) {
            logger.error("Weixin server connection timed out.");
        } catch (Exception e) {
            logger.error("https request error:{}", e);
        }
        return jsonObject;
    }

    private static final String token = "yunchongba";

    public static boolean validate(String signature, String timestamp, String nonce) {
        String[] tmpArr = {token, timestamp, nonce};
        Arrays.sort(tmpArr);
        String tmpStr = ArrayToString(tmpArr);
        tmpStr = SHA1Encode(tmpStr);
        if (tmpStr.equalsIgnoreCase(signature)) {
            return true;
        } else {
            return false;
        }
    }


    //数组转字符串
    public static String ArrayToString(String[] arr) {
        StringBuffer bf = new StringBuffer();
        for (int i = 0; i < arr.length; i++) {
            bf.append(arr[i]);
        }
        return bf.toString();
    }

    //sha1加密
    public static String SHA1Encode(String sourceString) {
        String resultString = null;
        try {
            resultString = new String(sourceString);
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            resultString = byte2hexString(md.digest(resultString.getBytes()));
        } catch (Exception ex) {
        }
        return resultString;
    }

    public static String byte2hexString(byte[] bytes) {
        StringBuffer buf = new StringBuffer(bytes.length * 2);
        for (int i = 0; i < bytes.length; i++) {
            if (((int) bytes[i] & 0xff) < 0x10) {
                buf.append("0");
            }
            buf.append(Long.toString((int) bytes[i] & 0xff, 16));
        }
        return buf.toString().toUpperCase();
    }

    /**
     * <p>传值</p>
     *
     * @param request 当前post值
     *                return String
     */
    public static String getStringPostWeixin(HttpServletRequest request) {
        StringBuilder buffer = new StringBuilder();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(request.getInputStream(), "UTF-8"));
            String line = null;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("request -> InputStream 异常");
        } finally {
            if (null != reader) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    logger.error("IO 异常");
                }
            }
        }
        return buffer.toString();
    }


    /**
     * <p>传入数据转为document</p>
     *
     * @param domStr 数据字符串
     *               reuturn document
     */
    public static Document automatismRestoreDocument(String domStr) {
        Document document = null;
        if (null != domStr && !domStr.isEmpty()) {
            try {
                logger.info("微信---传入数据str" + domStr);
                document = DocumentHelper.parseText(domStr);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (null == document) {
                logger.error("微信---传入数据有误");
                return null;
            }
        }
        return document;
    }

    /**
     * <p>自动回复</p>
     *
     * @throws Throwable
     */

    public static String automatismRestoreEntity(Map<String, Object> requestMap) throws Throwable {
        String resultStr = "";
        // 发送方帐号（open_id）
        String fromUsername = (String) requestMap.get("FromUserName");

        // 开发者微信号
        String toUserName = (String) requestMap.get("ToUserName");

        //消息创建时间 （整型）
        String createTime = (String) requestMap.get("CreateTime");

        //请求内容
        String content = (String) requestMap.get("Content");

        // 请求的消息类型
        String msgType = (String) requestMap.get("MsgType");

        //请求的事件类型
        String event = (String) requestMap.get("Event");

        String textTpl = "<xml>" +
                "<ToUserName><![CDATA[%1$s]]></ToUserName>" +
                "<FromUserName><![CDATA[%2$s]]></FromUserName>" +
                "<CreateTime>%3$s</CreateTime>" +
                "<MsgType><![CDATA[%4$s]]></MsgType>" +
                "<Content><![CDATA[%5$s]]></Content>" +
                "</xml>";
        String contentStr = "";
        switch (returnTypeNumber(msgType)) {
            case 1:
                if (null != content && !content.equals("")) {
                    switch (content) {
                        case "1":
                            contentStr = MessageReply.REPLY_ONE;
                            break;
                        case "2":
                            contentStr = MessageReply.REPLY_TWO;
                            break;
                        case "3":
                            contentStr = MessageReply.REPLY_THREE;
                            break;
                        case "4":
                            contentStr = MessageReply.REPLY_FOUR;
                            break;
                        case "5":
                            contentStr = MessageReply.REPLY_FIVE;
                            break;
                        case "6":
                            contentStr = MessageReply.REPLY_SIX;
                            break;
                        case "7":
                            contentStr = MessageReply.REPLY_SEVEN;
                            break;
                        case "8":
                            contentStr = MessageReply.REPLY_EIGHT;
                            break;
                        default:
                            contentStr = MessageReply.REPLY_OTHERS;
                            break;
                    }
                }
                break;
            case 2:
                if (null != event && event.equals("subscribe")) {
                    contentStr = MessageReply.FOLLOW;
                }
                break;
        }
        resultStr = textTpl.format(textTpl, fromUsername, toUserName, createTime, "text", contentStr);
        return resultStr;
    }

    /**
     * <p>1、text 2、event</p>
     *
     * @param type
     * @return
     */

    public static int returnTypeNumber(String type) {
        int i = 0;
        if (null != type && !type.isEmpty()) {
            if (type.equals("text")) {
                i = 1;
            } else if (type.equals("event")) {
                i = 2;
            }
        }
        return i;
    }
}