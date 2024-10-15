package com.example.matdongsanserver.common.auth.kakao;

import com.example.matdongsanserver.domain.member.entity.Member;
import com.example.matdongsanserver.domain.member.entity.Role;
import com.example.matdongsanserver.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class KakaoMemberDetailsService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;

    @Transactional
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        KakaoUserInfo kakaoUserInfo = new KakaoUserInfo(oAuth2User.getAttributes());

        //member 엔티티 확정시에 수정 필요
        Member member = memberRepository.findByEmail(kakaoUserInfo.getEmail())
                .orElseGet(() ->
                        memberRepository.save(
                                Member.builder()
                                        .email(kakaoUserInfo.getEmail())
                                        .nickname(kakaoUserInfo.getNickname())
                                        .profileImage(kakaoUserInfo.getProfileImage())
                                        .role(Role.USER)
                                        .build()
                        )
                );
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(member.getRole().name());

        return new KakaoMemberDetails(String.valueOf(member.getEmail()),
                Collections.singletonList(authority),
                oAuth2User.getAttributes());
    }
}
