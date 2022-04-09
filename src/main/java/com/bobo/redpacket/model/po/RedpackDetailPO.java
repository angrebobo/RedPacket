package com.bobo.redpacket.model.po;

import com.baomidou.mybatisplus.annotations.TableField;
import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author: HuangSiBo
 * @Description: 红包明细，用来描述用户抢到的红包金额
 * @Data: Created in 23:47 2021/9/19
 */

@Data
@TableName("t_redpack_detail")
public class RedpackDetailPO {

    @TableId
    private Long id;

    /**
     * 红包金额
     */
    @TableField("amount")
    private BigDecimal amount;

    /**
     * 用户编号
     */
    @TableField("user_id")
    private Long userID;

    /**
     * 红包编号
     */
    @TableField("redpack_id")
    private Long repackID;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;
}
