package com.github.czsurvey.web.controller;

import com.github.czsurvey.common.exception.BadRequestAlertException;
import com.github.czsurvey.common.exception.InvalidParameterException;
import com.github.czsurvey.common.util.HeaderUtil;
import com.github.czsurvey.project.entity.Survey;
import com.github.czsurvey.project.entity.enums.ProjectStatus;
import com.github.czsurvey.project.request.SurveyStatusRequest;
import com.github.czsurvey.project.request.SurveySummaryRequest;
import com.github.czsurvey.project.response.SurveyDetailResponse;
import com.github.czsurvey.project.response.SurveyTerseStatResponse;
import com.github.czsurvey.project.service.SurveyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author YanYu
 */
@RestController
@RequestMapping("/api/survey")
@RequiredArgsConstructor
public class SurveyController {

    private final SurveyService surveyService;

    @PostMapping
    public ResponseEntity<?> createSurvey(@RequestBody @Valid FolderIdRequest folderIdRequest) throws URISyntaxException {
        Survey survey = surveyService.createSurvey(folderIdRequest.folderId());
        return ResponseEntity
            .created(new URI("/api/survey/" + survey.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(true, Survey.entityName(), survey.getId().toString()))
            .body(survey);
    }

    @GetMapping("/{surveyId}")
    public ResponseEntity<?> getSurvey(@PathVariable Long surveyId) {
        if (surveyId == null) {
            throw new  InvalidParameterException("问卷ID不能为空", Survey.entityName());
        }
        Survey survey = surveyService.getSurvey(surveyId);
        return ResponseEntity.ok(survey);
    }

    @GetMapping("/detail")
    public ResponseEntity<?> getSurveyDetail(Long surveyId) {
        if (surveyId == null) {
            throw new  InvalidParameterException("问卷ID不能为空", Survey.entityName());
        }
        SurveyDetailResponse survey = surveyService.getSurveyDetail(surveyId);
        return ResponseEntity.ok(survey);
    }

   @PutMapping("/{surveyId}")
   public ResponseEntity<?> updateSurvey(
       @PathVariable Long surveyId,
       @Valid @RequestBody SurveySummaryRequest surveySummaryRequest
   ) {
       if (surveyId == null) {
           throw new BadRequestAlertException("问卷id不能为空", Survey.entityName(), "id_null");
       }
       Survey result = surveyService.updateSurvey(surveyId, surveySummaryRequest);
       return ResponseEntity
           .ok()
           .headers(HeaderUtil.createEntityUpdateAlert(true, Survey.entityName(), surveyId.toString()))
           .body(result);
   }

   @PutMapping("/status")
   public ResponseEntity<?> updateSurveyStatus(@Valid @RequestBody SurveyStatusRequest surveyStatusRequest) {
       ProjectStatus status = surveyService.updateSurveyStatus(surveyStatusRequest);
       return ResponseEntity.ok(status);
   }

   @PutMapping("/{surveyId}/title")
   public ResponseEntity<?> renameSurveyTitle(@PathVariable Long surveyId, @Valid @RequestBody SurveyTitleRequest surveyTitleRequest) {
        surveyService.renameSurveyTitle(surveyId, surveyTitleRequest.surveyTitle());
        return ResponseEntity.ok().build();
   }

   @GetMapping("/answerState/{surveyId}")
   public ResponseEntity<?> getSurveyAnswerState(@PathVariable Long surveyId) {
        if (surveyId == null) {
            throw new BadRequestAlertException("问卷id不能为空", Survey.entityName(), "id_null");
        }
        return ResponseEntity.ok(surveyService.getSurveyAnswerState(surveyId));
   }

   @DeleteMapping("/{surveyId}/page/{pageKey}")
   public ResponseEntity<?> deleteSurveyPage(@PathVariable Long surveyId, @PathVariable String pageKey) {
        surveyService.deleteSurveyPage(surveyId, pageKey);
        return ResponseEntity.ok().build();
   }

   @GetMapping("/{surveyId}/terseStat")
   public ResponseEntity<?> getTerseStat(@PathVariable Long surveyId) {
       SurveyTerseStatResponse result = surveyService.getTerseStat(surveyId);
       return ResponseEntity.ok(result);
   }

    public record FolderIdRequest(@NotNull Long folderId) {}

    public record SurveyTitleRequest(@NotBlank @Size(min = 1, max = 256) String surveyTitle) {}
}
