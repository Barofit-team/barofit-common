package com.barofit.testsupport;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * 테스트용 JWT 키를 임시 파일로 만든다. identity가 토큰을 서명하고 다른 서비스들이 같은 공개키로 검증하는 상황을 재현하기 위해, 토큰을 발급/검증하는 모든 레포가
 * 이 클래스를 공통으로 재사용한다.
 */
public record BarofitTestJwtKeys(
    Path privateKeyPath, Path publicJwksPath, String currentKid, KeyPair keyPair) {

  public static BarofitTestJwtKeys create(String kid) {
    return create(kid, generateKeyPair(), List.of());
  }

  /**
   * @param extraPublicKeys 키 교체 재현용 추가 공개키
   */
  public static BarofitTestJwtKeys create(
      String kid, KeyPair keyPair, List<RSAKey> extraPublicKeys) {
    try {
      Path dir = Files.createTempDirectory("barofit-jwt");
      Path privateKey = dir.resolve("jwt-private.pem");
      Path jwks = dir.resolve("jwt-jwks.json");
      Files.writeString(privateKey, toPkcs8Pem(keyPair));
      RSAKey publicKey = new RSAKey.Builder((RSAPublicKey) keyPair.getPublic()).keyID(kid).build();
      List<JWK> keys = new ArrayList<>(extraPublicKeys);
      keys.add(publicKey);
      Files.writeString(jwks, new JWKSet(keys).toString(true));
      return new BarofitTestJwtKeys(privateKey, jwks, kid, keyPair);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public static KeyPair generateKeyPair() {
    try {
      KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
      generator.initialize(2048);
      return generator.generateKeyPair();
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }

  private static String toPkcs8Pem(KeyPair keyPair) {
    String base64 =
        Base64.getMimeEncoder(64, "\n".getBytes())
            .encodeToString(keyPair.getPrivate().getEncoded());
    return "-----BEGIN PRIVATE KEY-----\n" + base64 + "\n-----END PRIVATE KEY-----\n";
  }
}
