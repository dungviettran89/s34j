package us.cuatoi.s34jserver.core;

public class S3Exception extends RuntimeException {
    private int statusCode = 500;
    private String name = "InternalError";
    private String description = "Internal error. Please try again";

    //Constructors

    public S3Exception(ErrorCode errorCode) {
        this(errorCode, errorCode.getDescription());
    }

    //Constructors

    public S3Exception(ErrorCode errorCode, String description) {
        this.statusCode = errorCode.getStatusCode();
        this.name = errorCode.getName();
        this.description = description;
    }

    //Getters
    public int getStatusCode() {
        return statusCode;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    //Setters
    public S3Exception setStatusCode(int statusCode) {
        this.statusCode = statusCode;
        return this;
    }

    public S3Exception setName(String name) {
        this.name = name;
        return this;
    }

    public S3Exception setDescription(String description) {
        this.description = description;
        return this;
    }


}
