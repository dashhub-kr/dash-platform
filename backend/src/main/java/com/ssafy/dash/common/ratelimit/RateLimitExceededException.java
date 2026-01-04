package com.ssafy.dash.common.ratelimit;

import com.ssafy.dash.common.exception.BusinessException;
import com.ssafy.dash.common.exception.ErrorCode;

public class RateLimitExceededException extends BusinessException {

    public RateLimitExceededException() {
        super(ErrorCode.TOO_MANY_REQUESTS);
    }
}
