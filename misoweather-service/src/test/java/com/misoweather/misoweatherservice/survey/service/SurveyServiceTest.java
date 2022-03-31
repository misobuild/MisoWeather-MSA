package com.misoweather.misoweatherservice.survey.service;

import com.misoweather.misoweatherservice.domain.survey.AnswerRepository;
import com.misoweather.misoweatherservice.domain.survey.Survey;
import com.misoweather.misoweatherservice.domain.survey.SurveyRepository;
import com.misoweather.misoweatherservice.global.api.ListDto;
import com.misoweather.misoweatherservice.global.constants.HttpStatusEnum;
import com.misoweather.misoweatherservice.global.exception.ApiCustomException;
import com.misoweather.misoweatherservice.survey.dto.AnswerStatusDto;
import com.misoweather.misoweatherservice.survey.dto.AnswerSurveyJoinDto;
import com.misoweather.misoweatherservice.survey.dto.AnswerSurveyJoinDtoBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

@ExtendWith(MockitoExtension.class)
@DisplayName("CommentService 테스트")
public class SurveyServiceTest {

    @Mock private SurveyRepository surveyRepository;
    @Mock private AnswerRepository answerRepository;
    @InjectMocks private SurveyService surveyService;

    @BeforeEach
    void setUp(){
        this.surveyService = new SurveyService(surveyRepository, answerRepository);
    }

    @Test
    @DisplayName("성공: getAnswerList() <Long>surveyId를 통해 List<AnswerSurveyJoinDto> 찾아 반환한다.")
    void getAnswerList(){
        // given
        Long givenSurveyId = 99999L;
        AnswerSurveyJoinDto givenAnswerSurveyJoinDto = AnswerSurveyJoinDtoBuilder
                .build(9999L, "test answer dsecrpiton", "test answer", givenSurveyId, "test survey description", "test survey title");
        given(answerRepository.findAnswerSurveyJoinBySurveyId(anyLong())).willReturn(List.of(givenAnswerSurveyJoinDto));

        // when
        ListDto<AnswerSurveyJoinDto> actual = surveyService.getAnswerList(givenSurveyId);

        // then
        assertThat(actual.getResponseList().get(0).getSurveyId(), is(givenSurveyId));
    }

    @Test
    @DisplayName("분기 테스트: getAnswerList() survey 존재하지 않으면 에러 발생시킨다.")
    void getAnswerListWhenListNull(){
        // given
        Long givenSurveyId = 99999L;
        given(answerRepository.findAnswerSurveyJoinBySurveyId(anyLong())).willReturn(List.of());

        // when, then
        assertThatThrownBy(() -> surveyService.getAnswerList(givenSurveyId))
                .isInstanceOf(ApiCustomException.class)
                .hasMessageContaining(HttpStatusEnum.NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("성공: getAllSurveyId() 모든 서베이를 찾아서 리스트로 반환한다")
    void getAllSurveyId(){
        // given
        Survey givenSurvey = spy(Survey.class);
        doReturn(9999L).when(givenSurvey).getId();
        given(surveyRepository.findAll()).willReturn(List.of(givenSurvey));

        // when
        List<Long> actual = surveyService.getAllSurveyId();

        // then
        assertThat(actual.get(0), is(9999L));
    }


    @Test
    @DisplayName("성공: 답변하지 않은 List<Long>) 리스트에 대해 null로 찬 List<AnserStatusDto> 만들어 리턴한다.")
    void buildAnswerStatusNullDtoList(){
        // given
        List<Long> givenSurveyIdList = List.of(99999L);

        // when
        List<AnswerStatusDto> actual = surveyService.buildAnswerStatusNullDtoList(givenSurveyIdList);

        // then
        assertThat(actual.get(0).getSurveyId(), is(99999L));
    }


    @Test
    @DisplayName("List<AnswerStatusDto>를 ListDto<AnswerStatusDto>로 변환하여 리턴한다.")
    void buildAnswerStatusResponseDtoList(){
        // given
        List<AnswerStatusDto> givenAnswerStatusDtoList = List.of(AnswerStatusDto.builder().surveyId(99999L).build());

        // when
        ListDto<AnswerStatusDto> actual = surveyService.buildAnswerStatusResponseDtoList(givenAnswerStatusDtoList);

        // then
        assertThat(actual.getResponseList().get(0).getSurveyId(), is(99999L));
    }
}