package com.ycb.app.provider.controller;

import com.ycb.app.provider.cache.RedisService;
import com.ycb.app.provider.mapper.OrderMapper;
import com.ycb.app.provider.mapper.ProblemFeedbackMapper;
import com.ycb.app.provider.mapper.UserMapper;
import com.ycb.app.provider.utils.JsonUtils;
import com.ycb.app.provider.vo.ProblemFeedback;
import com.ycb.app.provider.vo.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by duxinyuan on 17-10-20.
 */
@RestController
@RequestMapping("problem")
public class ProblemFeedbackController {

    public static final Logger logger = LoggerFactory.getLogger(ProblemFeedbackController.class);

    @Autowired
    private RedisService redisService;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private ProblemFeedbackMapper problemFeedbackMapper;

    @RequestMapping(value = "/orderProblem", method = RequestMethod.POST)
    public String orderProblem(@RequestParam("session") String session,
                               @RequestParam("service") String service,
                               @RequestParam("problem_id") Integer problemId,
                               @RequestParam("message") String message,
                               @RequestParam("orderid") String orderid,
                               @RequestParam("img") String photo,
                               @RequestParam("same_station") Integer identical) throws Exception {

        Map<String, Object> bacMap = new HashMap<>();

        if ("order".equals(service)) {

            //根据订单 判断提交反馈次数 超过2次禁止提交
            Integer feedbackNum = this.problemFeedbackMapper.queryOrderFeedbackNumByOrderid(orderid);
            if (1 < feedbackNum) {
                Map<String, Object> data = new HashMap<>();
                data.put("over_submit", true);
                bacMap.put("data", data);
                bacMap.put("code", 1);
                bacMap.put("msg", "提交失败，订单反馈次数超出上限");
                return JsonUtils.writeValueAsString(bacMap);
            }

            String openid = redisService.getKeyValue(session);
            User user = userMapper.findUserIdByOpenid(openid);

            //生成反馈编号
            String date = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
            Random random = new Random();
            StringBuilder sb = new StringBuilder(4);
            for (int i = 0; i < 4; i++) {
                sb.append(random.nextInt(10));
            }
            //反馈编号格式为：yyyyMMddHHmmss+随机数
            String feedbackId = date + sb.toString();
            //获取电池id
            String batteryRfid = this.orderMapper.findBatteryRfidByOrderId(orderid);

            ProblemFeedback problemFeedback = new ProblemFeedback();
            problemFeedback.setCreatedBy("SYS:orderProblem");
            problemFeedback.setFeedbackId(feedbackId);
            problemFeedback.setUserId(user.getId().toString());
            problemFeedback.setBatteryId(batteryRfid);
            problemFeedback.setIdentical(identical);
            problemFeedback.setMessage(message);
            problemFeedback.setOrderid(orderid);
            problemFeedback.setPhoto(photo);
            problemFeedback.setProblemId(problemId);
            problemFeedback.setStatus(0); // 0.未处理 1.处理中 2.已处理
            //保存订单反馈
            this.problemFeedbackMapper.insertOrderProblem(problemFeedback);

            Map<String, Object> data = new HashMap<>();
            data.put("over_submit", false);
            data.put("feedback_id", feedbackId);
            bacMap.put("data", data);
            bacMap.put("code", 0);
            bacMap.put("msg", "提交成功");

        } else {
            bacMap.put("data", null);
            bacMap.put("code", 1);
            bacMap.put("msg", "提交失败，请选择正确的service");
        }
        return JsonUtils.writeValueAsString(bacMap);
    }

}
