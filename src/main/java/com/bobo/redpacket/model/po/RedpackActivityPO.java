package com.bobo.redpacket.model.po;

import com.baomidou.mybatisplus.annotations.TableField;
import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.baomidou.mybatisplus.annotations.Version;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author: HuangSiBo
 * @Description: 红包活动
 * @Data: Created in 23:29 2021/9/19
 */
@Data
@TableName("t_redpack_activity")
public class RedpackActivityPO {

    @TableId("id")
    private Long id;

    /**
     * 红包总金额
     */
    @TableField("total_amount")
    private BigDecimal totalAmount;

    /**
     * 红包剩余金额
     */
    @TableField("surplus_amount")
    private BigDecimal surplusAmount;

    /**
     * 红包总个数
     */
    @TableField("total")
    private Integer total;

    /**
     * 红包剩余个数
     */
    @TableField("surplus_total")
    private Integer surplusTotal;

    /**
     * 版本号
     */
    @TableField("version")
    @Version
    private Integer version;
}
