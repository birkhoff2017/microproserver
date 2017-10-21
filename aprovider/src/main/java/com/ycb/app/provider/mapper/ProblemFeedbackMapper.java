package com.ycb.app.provider.mapper;

import com.ycb.app.provider.vo.ProblemFeedback;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * Created by duxinyuan on 2017/10/20.
 */

@Mapper
public interface ProblemFeedbackMapper {

    @Insert("INSERT INTO ycb_mcs_order_problem" +
            "(createdBy,createdDate,optlock,feedback_id,user_id,battery_id,identical,message,orderid,photo,problem_id,status) " +
            "VALUES(#{createdBy},NOW(),#{version},#{feedbackId},#{userId},#{batteryId},#{identical},#{message},#{orderid},#{photo},#{problemId},#{status})")
    void insertOrderProblem(ProblemFeedback problemFeedback);

    @Select("SELECT COUNT(1) FROM ycb_mcs_order_problem o WHERE o.orderid = #{orderid}")
    Integer queryOrderFeedbackNumByOrderid(String orderid);
}
