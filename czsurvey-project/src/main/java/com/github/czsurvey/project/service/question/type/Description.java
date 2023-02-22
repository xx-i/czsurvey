package com.github.czsurvey.project.service.question.type;

import com.github.czsurvey.project.service.question.QuestionType;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@Validated
public class Description implements QuestionType<Void> {

    public static final String TYPE = "DESCRIPTION";

    @Override
    public String getQuestionType() {
        return TYPE;
    }
}
