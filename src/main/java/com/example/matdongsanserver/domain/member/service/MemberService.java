package com.example.matdongsanserver.domain.member.service;

import com.example.matdongsanserver.domain.member.repository.ChildRepository;
import com.example.matdongsanserver.domain.member.repository.FollowRepository;
import com.example.matdongsanserver.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final ChildRepository childRepository;
    private final FollowRepository followRepository;
}
