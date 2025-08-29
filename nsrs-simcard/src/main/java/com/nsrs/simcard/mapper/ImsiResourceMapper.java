package com.nsrs.simcard.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nsrs.simcard.entity.ImsiResource;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * IMSI资源Mapper接口
 */
@Mapper
public interface ImsiResourceMapper extends BaseMapper<ImsiResource> {
    
    /**
     * 获取指定IMSI组下最大的IMSI号码
     * 使用LENGTH和CAST进行正确的数字排序
     * 
     * @param groupId 组ID
     * @return 最大IMSI号码
     */
    @Select("SELECT imsi FROM imsi_resource WHERE group_id = #{groupId} ORDER BY LENGTH(imsi) DESC, imsi DESC LIMIT 1")
    String getMaxImsiByGroupId(@Param("groupId") Long groupId);
    
    /**
     * 根据尾缀获取分表名
     * 
     * @param imsi IMSI号码
     * @return 分表名
     */
    default String getShardingTableName(String imsi) {
        if (imsi == null || imsi.length() < 1) {
            return "imsi_resource";
        }
        
        // 根据IMSI的最后一位或两位进行分表
        int suffix = Integer.parseInt(imsi.substring(imsi.length() - 1));
        return "imsi_resource_" + suffix;
    }
    
    /**
     * 批量插入IMSI资源
     * 
     * @param resources IMSI资源列表
     * @return 受影响的行数
     */
    int batchInsert(@Param("list") List<ImsiResource> resources);
}