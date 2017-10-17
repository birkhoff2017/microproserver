package com.ycb.wpc.provider.mapper;

import com.ycb.wpc.provider.vo.ShopStation;
import com.ycb.wpc.provider.vo.Shop;
import com.ycb.wpc.provider.vo.Station;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by zhuhui on 17-7-26.
 */
@Mapper
public interface ShopMapper {

    //    @Select("Select shop.name, ss.address, ss.latitude, ss.longitude From ycb_mcs_shop shop, ycb_mcs_shop_station ss, ycb_mcs_station s " +
    //            "Where ss.shopid = shop.id And ss.station_id = s.id " +
    //            "And abs(ss.latitude - #{latitude}) < 50 " +
    //            "And abs(ss.longitude - #{longitude}) < 50 ")
    @Select("Select *,ss.title name From ycb_mcs_shop_station ss " +
            "Left Join ycb_mcs_station s " +
            "On ss.station_id = s.id " +
            "And abs(ss.latitude - #{latitude}) < 50 " +
            "And abs(ss.longitude - #{longitude}) < 50 ")
    @Results({
            @Result(property = "name", column = "name"),
            @Result(property = "shopStation", column = "station_id", many = @Many(select = "ShopMapper.findStations"))
    })
    List<ShopStation> findShops(@Param("latitude") String latitude, @Param("longitude") String longitude);

    @Select("Select * From ycb_mcs_station s Where id = #{stationId} ")
    @Results(id = "station", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "title", column = "title"),
            @Result(property = "usable", column = "usable"),
            @Result(property = "empty", column = "empty")
    })
    List<Station> findStations(@Param("stationId") Long stationId);

    @Select("Select * From ycb_mcs_shop_station ss")
    @Results(id = "shopStation", value = {
            @Result(property = "address", column = "address"),
            @Result(property = "latitude", column = "latitude"),
            @Result(property = "longitude", column = "longitude")
    })
    List<ShopStation> findShopStations();

    @Select("SELECT s.* " +
            "FROM ycb_mcs_shop_station ss," +
            "ycb_mcs_shop s WHERE ss.shopid=s.id AND ss.station_id=#{sid}")
    Shop getShopInfoBySid(String sid);
    //获取押金金额
    @Select("SELECT s.defaultPay " +
            "FROM ycb_mcs_shop_station ss," +
            "ycb_mcs_shop s WHERE ss.shopid=s.id AND ss.station_id=#{sid}")
    BigDecimal getShopDefaultPayInfoBySid(String sid);

    @Select("Select * From ycb_mcs_shop shop, ycb_mcs_shop_station ss, ycb_mcs_station s " +
            "Where ss.shopid = shop.id And ss.station_id = s.id " +
            "AND ss.id = #{shopStationId}")
    @Results({
            @Result(property = "name", column = "shop.name"),
            @Result(property = "address", column = "ss.address"),
            @Result(property = "latitude", column = "ss.latitude"),
            @Result(property = "longitude", column = "ss.longitude"),
            @Result(property = "cost", column = "shop.cost"),
            @Result(property = "phone", column = "shop.phone"),
            @Result(property = "stime", column = "shop.stime"),
            @Result(property = "etime", column = "shop.etime"),
            @Result(property = "shopStation", column = "station_id", many = @Many(select = "ShopMapper.findStations"))
    })
    ShopStation findShopInfo(@Param("shopStationId") Long shopStationId);

    //根据店铺的id查询店铺信息
    @Select("Select name From ycb_mcs_shop shop where shop.id = #{shopId}")
    @Results(value = {
            @Result(property = "name",column = "name")
    })
    Shop findShopById(@Param("shopId") Long shopId);
}
