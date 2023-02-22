package com.github.czsurvey.web.controller;

import com.github.czsurvey.common.util.HeaderUtil;
import com.github.czsurvey.project.entity.SurveyLogic;
import com.github.czsurvey.project.service.SurveyLogicService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/survey/logic")
@RequiredArgsConstructor
public class SurveyLogicController {

    private final SurveyLogicService logicService;

    @PostMapping
    public ResponseEntity<?> createOrUpdateSurveyLogic(@Validated @RequestBody SurveyLogic surveyLogic) {
        SurveyLogic result = logicService.createOrUpdateSurveyLogic(surveyLogic);
        return ResponseEntity.ok().body(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> removeSurveyLogic(@PathVariable Long id) {
        logicService.removeById(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(true, SurveyLogic.entityName(), id.toString()))
            .build();
    }
}
