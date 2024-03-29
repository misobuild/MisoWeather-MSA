package com.misoweather.misoweatherservice.mapping.reader;

import com.misoweather.misoweatherservice.domain.member_survey_mapping.MemberSurveyMapping;
import com.misoweather.misoweatherservice.domain.survey.Answer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;


@ExtendWith(MockitoExtension.class)
@DisplayName("SurverReader 테스트")
public class SurveryReaderTest {
    private SurveyReader surveyReader;

    @BeforeEach
    void setUpGiven() {
        Answer givenAnswer = spy(Answer.class);
        doReturn("안녕하세요").when(givenAnswer).getContent();

        MemberSurveyMapping givenMemberSurveyMapping = MemberSurveyMapping.builder().answer(givenAnswer).build();
        MemberSurveyMapping givenMemberSurveyMappingTwo = MemberSurveyMapping.builder().answer(givenAnswer).build();
        List<MemberSurveyMapping> msmList = List.of(givenMemberSurveyMapping, givenMemberSurveyMappingTwo);

        this.surveyReader = new SurveyReader(msmList, 9999L, "테스트 서베이 설명", "테스트 서베이");
    }

    @Test
    @DisplayName("setInfoMap 테스트")
    void setInfoMapTest() {
        // when
        surveyReader.setInfoMap();

        // then
        HashMap<String, Integer> actual = surveyReader.getInfoMap();
        assertThat(actual.get("안녕하세요"), is(2));
    }

    @Test
    @DisplayName("setValues 테스트")
    void setValuesTest() {
        // when
        surveyReader.setInfoMap();
        surveyReader.setValues();

        // then
        assertThat(surveyReader.getKeyList().get(0), is("안녕하세요"));
        assertThat(surveyReader.getValueList().get(0), is(100));
    }
}
