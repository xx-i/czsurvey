package com.github.czsurvey.web.controller;

import com.github.czsurvey.common.exception.InvalidParameterException;
import com.github.czsurvey.common.util.HeaderUtil;
import com.github.czsurvey.common.util.PaginationUtil;
import com.github.czsurvey.project.entity.SurveyAnswer;
import com.github.czsurvey.project.request.BatchChangeAnswerValidRequest;
import com.github.czsurvey.project.request.ChangeAnswerValidRequest;
import com.github.czsurvey.project.request.SurveyAnswerQueryRequest;
import com.github.czsurvey.project.request.SurveyAnswerRequest;
import com.github.czsurvey.project.response.SurveyAnswerResponse;
import com.github.czsurvey.project.service.SurveyAnswerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.constraints.NotEmpty;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/survey/answer")
public class SurveyAnswerController {

    private final SurveyAnswerService surveyAnswerService;

    @PostMapping
    public ResponseEntity<?> createAnswer(@RequestBody @Validated SurveyAnswerRequest surveyAnswerRequest) throws URISyntaxException {
        SurveyAnswer result = surveyAnswerService.createOrUpdateSurveyAnswer(surveyAnswerRequest);
        return ResponseEntity
            .created(new URI("/api/surveyQuestion/" + result.getSurveyId()))
            .headers(HeaderUtil.createEntityCreationAlert(true, SurveyAnswer.entityName(), result.getId().toString()))
            .body(result);
    }

    @GetMapping("/page")
    public ResponseEntity<?> pageAnswers(SurveyAnswerQueryRequest surveyAnswerQueryRequest, Pageable pageable) {
        Page<SurveyAnswerResponse> page = surveyAnswerService.pageAnswers(surveyAnswerQueryRequest, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @PutMapping("/valid")
    public ResponseEntity<?> changeAnswerIsValid(@RequestBody @Validated ChangeAnswerValidRequest request) {
        surveyAnswerService.changeAnswerIsValid(request);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/batch/valid")
    public ResponseEntity<?> batchChangeAnswerIdValid(@RequestBody @Validated BatchChangeAnswerValidRequest request) {
        surveyAnswerService.batchChangeAnswerIdValid(request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAnswer(@PathVariable Long id) {
        surveyAnswerService.deleteAnswer(id);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityDeletionAlert(true, SurveyAnswer.entityName(), id.toString()))
            .build();
    }

    @DeleteMapping
    public ResponseEntity<?> batchDeleteAnswers(@RequestBody @Validated BatchDeleteAnswersRequest batchDeleteAnswersRequest) {
        surveyAnswerService.batchDeleteAnswers(batchDeleteAnswersRequest.ids());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/clearData")
    public ResponseEntity<?> clearAllSurveyAnswerData(Long surveyId) {
        if (surveyId == null) {
            throw new InvalidParameterException("问卷ID不能为空", SurveyAnswer.entityName());
        }
        surveyAnswerService.clearAllSurveyAnswerData(surveyId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/last/{surveyId}")
    public ResponseEntity<?> getLoginUserLastAnswerBySurveyId(@PathVariable Long surveyId) {
        SurveyAnswer result = surveyAnswerService.getLoginUserLastAnswerBySurveyId(surveyId);
        return ResponseEntity.ok(result);
    }

    public record BatchDeleteAnswersRequest(@NotEmpty List<Long> ids) {}
}
