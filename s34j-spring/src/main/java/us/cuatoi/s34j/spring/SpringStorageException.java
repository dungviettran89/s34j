package us.cuatoi.s34j.spring;

import us.cuatoi.s34j.spring.dto.ErrorCode;

public class SpringStorageException extends RuntimeException {
    private ErrorCode errorCode;
    private String description;

    public SpringStorageException(ErrorCode errorCode) {
        this(errorCode, errorCode.getDescription());
    }

    public SpringStorageException(ErrorCode errorCode, String description) {
        this.errorCode = errorCode;
        this.description = description;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public String getDescription() {
        return description;
    }
}
