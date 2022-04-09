package com.bobo.redpacket.common.utils;

import com.bobo.redpacket.common.RspCode;
import com.bobo.redpacket.exception.LockInterruptedException;
import com.bobo.redpacket.exception.RedpackException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

@Slf4j
public final class RedpackExceptionTranslator {

	private RedpackExceptionTranslator() {
		super();
	}

	public static RedpackException convertRedpackException(Exception e) {
		Assert.notNull(e, "Exception must not be null");
		if (e instanceof RedpackException) {
			return (RedpackException) e;
		}
		if (e instanceof LockInterruptedException) {
			return new RedpackException(RspCode.ERROR);
		}
		log.error("系统错误，未知的异常：", e);
		return new RedpackException(RspCode.ERROR);
	}

}
