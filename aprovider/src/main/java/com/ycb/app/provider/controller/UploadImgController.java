package com.ycb.app.provider.controller;

import com.google.gson.Gson;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import com.ycb.app.provider.cache.RedisService;
import com.ycb.app.provider.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by duxinyuan on 17-8-7.
 */
@RestController
@RequestMapping("upload")
public class UploadImgController {

    public static final Logger logger = LoggerFactory.getLogger(UploadImgController.class);

    @Autowired
    private RedisService redisService;

    @Value("${bucketName}")
    private String bucketName;
    @Value("${domain}")
    private String domain;
    @Value("${ACCESS_KEY}")
    private String ACCESS_KEY;
    @Value("${SECRET_KEY}")
    private String SECRET_KEY;

    @RequestMapping(value = "/uploaFeedbackdImg", method = RequestMethod.POST)
    public String testQiuNiu(@RequestParam("file") MultipartFile[] multipartFile) throws Exception {

        Map<String, Object> bacMap = new HashMap<>();

        if (multipartFile.length > 0) {
            List<String> list = new ArrayList<String>();
            for (MultipartFile file : multipartFile) {
                //构造一个带指定Zone对象的配置类
                Configuration cfg = new Configuration(Zone.zone1());
                //...其他参数参考类注释
                UploadManager uploadManager = new UploadManager(cfg);
                //默认不指定key的情况下，以文件内容的hash值作为文件名
                String upToken = this.getUpToken();
                try {
                    String filenameExtension = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."), file.getOriginalFilename().length());
                    SimpleDateFormat time = new SimpleDateFormat("yyyy/MM/dd");
                    String key = time.format(new Date()) + "/" + UUID.randomUUID() + filenameExtension;
                    //上传
                    Response response = uploadManager.put(file.getInputStream(), key, upToken, null, null);
                    //解析上传成功的结果
                    DefaultPutRet putRet = new Gson().fromJson(response.bodyString(), DefaultPutRet.class);
                    System.out.println("图片key=" + putRet.key + "    ,   " + "图片hash=" + putRet.hash);
                    list.add(domain + "/" + putRet.key);

                } catch (QiniuException ex) {
                    System.err.println(ex.response.toString());
                    bacMap.put("data", null);
                    bacMap.put("code", 1);
                    bacMap.put("msg", "上传失败，系统异常。");
                    return JsonUtils.writeValueAsString(bacMap);
                }
            }
            bacMap.put("data", list);
            bacMap.put("code", 0);
            bacMap.put("msg", "上传成功");
        } else {
            bacMap.put("data", null);
            bacMap.put("code", 1);
            bacMap.put("msg", "上传失败，请选择照片");
        }
        return JsonUtils.writeValueAsString(bacMap);
    }

    public String getUpToken() {
        String QINIU_UPLOAD_TOKEN = redisService.getKeyValue("QINIU_UPLOAD_TOKEN");
        if (StringUtils.isEmpty(QINIU_UPLOAD_TOKEN)) {
            Auth auth = Auth.create(ACCESS_KEY, SECRET_KEY);
            StringMap putPolicy = new StringMap();
            putPolicy.put("returnBody", "{\"key\":\"$(key)\",\"hash\":\"$(etag)\",\"bucket\":\"$(bucket)\",\"fsize\":$(fsize)}");
            long expireSeconds = 3600;
            String upToken = auth.uploadToken(bucketName, null, expireSeconds, putPolicy);
            System.out.println(upToken);
            // 将QINIU_UPLOAD_TOKEN存入Redis,存放时间为3600秒
            redisService.setKeyValueTimeout("QINIU_UPLOAD_TOKEN", upToken.trim(), expireSeconds);
            return upToken.trim();
        }
        return QINIU_UPLOAD_TOKEN.trim();
    }

}
