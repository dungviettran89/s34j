package us.cuatoi.s34j.spring.auth;

public interface AuthenticationProvider {
    String getSecretKey(String accessKey);
}
