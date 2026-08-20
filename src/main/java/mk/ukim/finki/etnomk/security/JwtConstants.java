package mk.ukim.finki.etnomk.security;

public class JwtConstants {

    public static final Long EXPIRATION_TIME = 86400000L;
    public static final String HEADER = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";

    private JwtConstants() {
    }
}
