package us.cuatoi.s34j.spring;

import us.cuatoi.s34j.spring.dto.ErrorCode;

public class SpringStorageException extends RuntimeException {
    private ErrorCode errorCode;

    public SpringStorageException(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

}
