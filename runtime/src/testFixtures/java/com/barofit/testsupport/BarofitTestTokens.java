package com.barofit.testsupport;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

/**
 * 테스트용 Access Token. claim을 바꿔 위조·만료 토큰을 만든다.
 *
 * <p>iss/aud/role claim 이름은 {@code barofit-common/runtime}의 {@code SecurityConfig}가 검증하는 값과 맞춰뒀다
 * (원본은 identity의 {@code BarofitTokenClaims}에 있고, 이 레포에서 직접 참조하진 않는다 — common이 identity에 의존하는 역방향
 * 의존을 만들지 않기 위해 같은 값을 독립적으로 들고 있다).
 */
public final class BarofitTestTokens {

  private static final String ISSUER = "barofit-identity";
  private static final String AUDIENCE = "barofit-api";
  private static final String ROLE_CLAIM = "role";

  private BarofitTestTokens() {}

  public static String accessToken(UUID userId, Instant issuedAt) {
    return accessToken(BarofitJwtKeyTestSupport.KEYS.keyPair(), userId, issuedAt, claims -> {});
  }

  public static String accessToken(
      KeyPair keyPair, UUID userId, Instant issuedAt, Consumer<JwtClaimsSet.Builder> customizer) {
    JwtClaimsSet.Builder claims =
        JwtClaimsSet.builder()
            .issuer(ISSUER)
            .audience(List.of(AUDIENCE))
            .subject(userId.toString())
            .issuedAt(issuedAt)
            .expiresAt(issuedAt.plusSeconds(15 * 60))
            .id(UUID.randomUUID().toString())
            .claim(ROLE_CLAIM, "USER");
    customizer.accept(claims);
    BarofitTestJwtKeys keys = BarofitJwtKeyTestSupport.KEYS;
    RSAKey signingKey =
        new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
            .privateKey((RSAPrivateKey) keyPair.getPrivate())
            .keyID(keys.currentKid())
            .build();
    var encoder = new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(signingKey)));
    JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256).keyId(keys.currentKid()).build();
    return encoder.encode(JwtEncoderParameters.from(header, claims.build())).getTokenValue();
  }
}
