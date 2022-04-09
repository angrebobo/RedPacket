package com.bobo.redpacket.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author: HuangSiBo
 * @Description: 发红包数据格式，红包金额，红包个数，用户ID
 * @Data: Created in 15:00 2021/9/20
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateActivityDTO {

    private BigDecimal totalAmount;
    private Long userID;
    private Integer total;
}
