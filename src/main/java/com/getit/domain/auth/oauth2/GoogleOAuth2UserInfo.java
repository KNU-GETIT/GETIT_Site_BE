package com.getit.domain.auth.oauth2;

import java.util.Map;

/**
 * Google userinfo 응답에서 필요한 값만 뽑는다.
 *
 * @param providerId Google 계정의 sub. 이메일이 바뀌어도 변하지 않는 식별자다
 */
public record GoogleOAuth2UserInfo(
    String providerId,
    String email,
    String name,
    String profileImageUrl
) {

  private static final String ATTR_SUB = "sub";
  private static final String ATTR_EMAIL = "email";
  private static final String ATTR_NAME = "name";
  private static final String ATTR_PICTURE = "picture";

  public static GoogleOAuth2UserInfo from(Map<String, Object> attributes) {
    return new GoogleOAuth2UserInfo(
        asString(attributes.get(ATTR_SUB)),
        asString(attributes.get(ATTR_EMAIL)),
        asString(attributes.get(ATTR_NAME)),
        asString(attributes.get(ATTR_PICTURE))
    );
  }

  private static String asString(Object value) {
    return value == null ? null : String.valueOf(value);
  }
}
