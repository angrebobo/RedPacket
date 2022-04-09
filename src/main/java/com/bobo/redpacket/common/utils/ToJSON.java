package com.bobo.redpacket.common.utils;

import com.alibaba.fastjson.JSON;

public interface ToJSON {

    default String toJSON() {
        return JSON.toJSONString(this);
    }
}
