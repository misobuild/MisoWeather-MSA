package com.misoweather.misoweatherservice.service;

import com.misoweather.misoweatherservice.auth.JwtTokenProvider;
import com.misoweather.misoweatherservice.auth.KakaoOAuth;
import com.misoweather.misoweatherservice.constants.BigScaleEnum;
import com.misoweather.misoweatherservice.constants.HttpStatusEnum;
import com.misoweather.misoweatherservice.domain.member.Member;
import com.misoweather.misoweatherservice.domain.member.MemberRepository;
import com.misoweather.misoweatherservice.domain.member_region_mapping.MemberRegionMapping;
import com.misoweather.misoweatherservice.domain.nickname.*;
import com.misoweather.misoweatherservice.dto.request.member.LoginRequestDto;
import com.misoweather.misoweatherservice.dto.request.member.SignUpRequestDto;
import com.misoweather.misoweatherservice.dto.response.member.MemberInfoResponseDto;
import com.misoweather.misoweatherservice.dto.response.member.NicknameResponseDto;
import com.misoweather.misoweatherservice.exception.ApiCustomException;
import com.misoweather.misoweatherservice.utils.factory.ValidatorFactory;
import com.misoweather.misoweatherservice.utils.validator.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@RequiredArgsConstructor
@Service
public class MemberService {

    // TODO 필드 생성자 추천하지 않음
    private final MemberRepository memberRepository;
    private final AdjectiveRepository adjectiveRepository;
    private final AdverbRepository adverbRepository;
    private final EmojiRepository emojiRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public Member getMember(String socialId, String socialType) {
        Member member = memberRepository
                .findBySocialIdAndSocialType(socialId, socialType)
                .orElseThrow(() -> new ApiCustomException(HttpStatusEnum.NOT_FOUND));
        return member;
    }

    public NicknameResponseDto buildNickname() {
        // TODO RANDOM sql 사용하면 좋을 것 같다.
        Adjective adjective = adjectiveRepository.findById(getRandomId(adjectiveRepository.count()))
                .orElseThrow(() -> new ApiCustomException(HttpStatusEnum.NOT_FOUND));

        Adverb adverb = adverbRepository.findById(getRandomId(adverbRepository.count()))
                .orElseThrow(() -> new ApiCustomException(HttpStatusEnum.NOT_FOUND));

        Emoji emoji = emojiRepository.findById(getRandomId(emojiRepository.count()))
                .orElseThrow(() -> new ApiCustomException(HttpStatusEnum.NOT_FOUND));

        return NicknameResponseDto.builder()
                .nickname(adjective.getWord() + " " + adverb.getWord() + emoji.getWord())
                .emoji(emoji.getEmoji())
                .build();
    }

    public Member buildMemberAndSave(SignUpRequestDto signUpRequestDto) {
        Member member = Member.builder()
                .socialId(signUpRequestDto.getSocialId())
                .socialType(signUpRequestDto.getSocialType())
                .emoji(signUpRequestDto.getEmoji())
                .nickname(signUpRequestDto.getNickname())
                .build();

        return memberRepository.save(member);
    }

    public String reissue(LoginRequestDto loginRequestDto, String socialToken) {
        // checkMember
        Validator validator = ValidatorFactory
                .of(loginRequestDto.getSocialId(), loginRequestDto.getSocialType(), socialToken);
        Boolean temp = validator.valid();
        if (temp.equals(Boolean.FALSE)) {
            throw new ApiCustomException(HttpStatusEnum.BAD_REQUEST);
        }

        Member member = memberRepository
                .findBySocialIdAndSocialType(loginRequestDto.getSocialId(), loginRequestDto.getSocialType())
                .orElseThrow(() -> new ApiCustomException(HttpStatusEnum.NOT_FOUND));

        return jwtTokenProvider
                .createToken(Long.toString(member.getMemberId()), member.getSocialId(), member.getSocialType());
    }

    public MemberInfoResponseDto buildMemberInfoResponse(Member member, MemberRegionMapping memberRegionMapping) {
        return MemberInfoResponseDto.builder()
                .emoji(member.getEmoji())
                .nickname(member.getNickname())
                .regionId(memberRegionMapping.getRegion().getId())
                .regionName(BigScaleEnum
                        .getEnum(memberRegionMapping.getRegion().getBigScale()).toString())
                .build();
    }

    public Long getRandomId(Long number) {
        int randomNumber = ThreadLocalRandom
                .current()
                .nextInt(1, Long.valueOf(number).intValue() + 1);

        return Long.valueOf(randomNumber);
    }

    public void checkToken(String socialId, String socialType, String socialToken) {
        Validator validator = ValidatorFactory.of(socialId, socialType, socialToken);
        if (!validator.valid()) throw new ApiCustomException(HttpStatusEnum.BAD_REQUEST);
    }

    public void checkExistence(String socialId, String socialType, String nickname) {
        memberRepository.findBySocialIdAndSocialType(socialId, socialType)
                .ifPresent(m -> {
                    throw new ApiCustomException(HttpStatusEnum.CONFLICT);
                });
        memberRepository.findByNickname(nickname)
                .ifPresent(m -> {
                    throw new ApiCustomException(HttpStatusEnum.CONFLICT);
                });
    }


    public void deleteMember(Member member) {
        memberRepository.delete(member);
    }
}
