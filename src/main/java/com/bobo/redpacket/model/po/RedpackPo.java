package com.bobo.redpacket.model.po;

import com.baomidou.mybatisplus.annotations.TableField;
import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.baomidou.mybatisplus.annotations.Version;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author: HuangSiBo
 * @Description: 生成的红包
 * @Data: Created in 13:29 2021/9/20
 */

@Data
@TableName("t_redpack")
public class RedpackPo implements Serializable {

    public static final int ENABLE = 1;

    public static final int DISABLE = 2;

    @TableId
    private Long id;

    @TableField("activity_id")
    private Long activity_id;

    /**
     * 红包金额
     */
    @TableField("amount")
    private BigDecimal amount;

    /**
     * 红包状态，1表示可用，2表示不可用
     */
    @TableField("status")
    private Integer status;

    /**
     * 版本号
     */
    @TableField("version")
    @Version
    private Integer version;
}
