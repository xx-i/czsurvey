package com.github.czsurvey.web.controller;

import com.github.czsurvey.common.exception.InvalidParameterException;
import com.github.czsurvey.common.util.HeaderUtil;
import com.github.czsurvey.common.valication.group.Group;
import com.github.czsurvey.project.entity.Project;
import com.github.czsurvey.project.entity.SurveyQuestion;
import com.github.czsurvey.project.request.SurveyQuestionRequest;
import com.github.czsurvey.project.request.SwapQuestionRequest;
import com.github.czsurvey.project.service.SurveyQuestionService;
import com.github.czsurvey.project.service.question.QuestionTypeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author YanYu
 */
@RestController
@RequestMapping("/api/survey")
@RequiredArgsConstructor
public class SurveyQuestionController {

    private final SurveyQuestionService questionService;

    @PostMapping("/question")
    public ResponseEntity<SurveyQuestion> createSurveyQuestion(@RequestBody @Validated(Group.Create.class) SurveyQuestionRequest questionRequest) throws URISyntaxException {
        validateSurveyQuestion(questionRequest);
        SurveyQuestion result = questionService.createOrUpdateSurveyQuestion(questionRequest, false);
        return ResponseEntity
            .created(new URI("/api/surveyQuestion/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(true, SurveyQuestion.entityName(), result.getId().toString()))
            .body(result);
    }

    @PutMapping("/question")
    public ResponseEntity<SurveyQuestion> updateSurveyQuestion(@RequestBody @Valid SurveyQuestionRequest questionRequest) {
        questionRequest.setPageKey(null);
        validateSurveyQuestion(questionRequest);
        SurveyQuestion result = questionService.createOrUpdateSurveyQuestion(questionRequest, true);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(true, SurveyQuestion.entityName(), result.getId().toString()))
            .body(result);
    }

    @PostMapping("/question/swapQuestionDisplayOrder")
    public ResponseEntity<?> swapQuestionDisplayOrder(@RequestBody @Validated SwapQuestionRequest swapQuestionRequest) {
        questionService.swapQuestionDisplayOrder(swapQuestionRequest);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{surveyId}/question/{questionKey}")
    public ResponseEntity<?> deleteQuestion(@PathVariable("surveyId") Long surveyId, @PathVariable("questionKey") String questionKey) {
        questionService.deleteQuestion(surveyId, questionKey);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityDeletionAlert(true, Project.entityName(), surveyId + "." + questionKey))
            .build();
    }

    private void validateSurveyQuestion(SurveyQuestionRequest questionRequest) {
        if (!QuestionTypeUtil.getQuestionTypes().contains(questionRequest.getType())) {
            throw new InvalidParameterException("问题类型不存在", SurveyQuestion.entityName());
        }
        if (QuestionTypeUtil.getInputModeQuestionTypes().contains(questionRequest.getType())) {
            if (questionRequest.getTitle() == null) {
                throw new InvalidParameterException("题目标题不能为空", SurveyQuestion.entityName());
            }
            if (questionRequest.getRequired() == null) {
                throw new InvalidParameterException("是否必填不能为空", SurveyQuestion.entityName());
            }
        }
    }
}
