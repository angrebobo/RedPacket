package com.bobo.redpacket.model.dto;

import com.google.common.base.Objects;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author: HuangSiBo
 * @Description:
 * @Data: Created in 19:29 2021/9/21
 */
@Data
public class RedpackPersistDTO implements Serializable {

    private Long id;

    private Long activiytID;

    private BigDecimal amount;

    private Integer status;

    private Integer version;

    private Long userID;

    private LocalDateTime createTime;

    @Override
    public boolean equals(Object o){
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RedpackPersistDTO that = (RedpackPersistDTO) o;
        return Objects.equal(id, that.id) &&
                Objects.equal(activiytID, that.activiytID) &&
                Objects.equal(amount, that.amount) &&
                Objects.equal(status, that.status) &&
                Objects.equal(version, that.version) &&
                Objects.equal(userID, that.userID) &&
                Objects.equal(createTime, that.createTime);
    }

    @Override
    public int hashCode(){
        return Objects.hashCode(id, activiytID, amount, status, version, userID, createTime);
    }
}
