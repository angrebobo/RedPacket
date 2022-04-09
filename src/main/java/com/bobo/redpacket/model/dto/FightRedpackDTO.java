package com.bobo.redpacket.model.dto;

import lombok.Data;

/**
 * @author: HuangSiBo
 * @Description: 抢红包数据格式
 * @Data: Created in 15:22 2021/9/20
 */

@Data
public class FightRedpackDTO {

    /**
     * 用户编号
     */
    private Long userID;

    /**
     * 活动编号
     */
    private Long activityID;
}
