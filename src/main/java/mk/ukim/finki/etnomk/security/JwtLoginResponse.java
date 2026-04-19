package mk.ukim.finki.etnomk.security;

public class JwtLoginResponse {

    private String token;
    private String tokenType;
    private long expiresInMs;

    public JwtLoginResponse() {
    }

    public JwtLoginResponse(String token, String tokenType, long expiresInMs) {
        this.token = token;
        this.tokenType = tokenType;
        this.expiresInMs = expiresInMs;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public long getExpiresInMs() {
        return expiresInMs;
    }

    public void setExpiresInMs(long expiresInMs) {
        this.expiresInMs = expiresInMs;
    }
}
