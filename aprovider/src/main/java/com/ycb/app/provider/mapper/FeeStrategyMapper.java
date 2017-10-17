package com.ycb.app.provider.mapper;

import com.ycb.app.provider.vo.FeeStrategy;
import org.apache.ibatis.annotations.*;

/**
 * Created by zhuhui on 17-8-28.
 */
@Mapper
public interface FeeStrategyMapper {
    @Select("Select * From ycb_mcs_fee_strategy " +
            "Where id = #{id}")
    @Results({
            @Result(property = "name", column = "name"),
            @Result(property = "freeTime", column = "free_time"),
            @Result(property = "freeUnit", column = "free_unit"),
            @Result(property = "fixedTime", column = "fixed_time"),
            @Result(property = "fixedUnit", column = "fixed_unit"),
            @Result(property = "fixed", column = "fixed"),
            @Result(property = "fee", column = "fee"),
            @Result(property = "feeUnit", column = "fee_unit"),
            @Result(property = "maxFeeTime", column = "max_fee_time"),
            @Result(property = "maxFeeUnit", column = "max_fee_unit"),
            @Result(property = "maxFee", column = "max_fee")
    })
    FeeStrategy findFeeStrategy(@Param("id") Long id);

    @Select("Select * From ycb_mcs_fee_strategy " +
            "Where name = '全局配置' " +
            "Limit 1")
    @Results({
            @Result(property = "name", column = "name"),
            @Result(property = "freeTime", column = "free_time"),
            @Result(property = "freeUnit", column = "free_unit"),
            @Result(property = "fixedTime", column = "fixed_time"),
            @Result(property = "fixedUnit", column = "fixed_unit"),
            @Result(property = "fixed", column = "fixed"),
            @Result(property = "fee", column = "fee"),
            @Result(property = "feeUnit", column = "fee_unit"),
            @Result(property = "maxFeeTime", column = "max_fee_time"),
            @Result(property = "maxFeeUnit", column = "max_fee_unit"),
            @Result(property = "maxFee", column = "max_fee")
    })
    FeeStrategy findGlobalFeeStrategy();

}
