package com.github.czsurvey.web.controller;

import com.github.czsurvey.project.entity.SurveySetting;
import com.github.czsurvey.project.service.SurveySettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author YanYu
 */
@RestController
@RequestMapping("/api/survey/setting")
@RequiredArgsConstructor
public class SurveySettingController {

    private final SurveySettingService surveySettingService;

    @GetMapping("/{surveyId}")
    public ResponseEntity<?> getSurveySetting(@PathVariable Long surveyId) {
        SurveySetting setting = surveySettingService.getSurveySetting(surveyId);
        return ResponseEntity.ok(setting);
    }

    @PutMapping
    public ResponseEntity<?> setSurveySetting(@RequestBody Map<String, Object> setting) {
        surveySettingService.setSurveySetting(setting);
        return ResponseEntity.ok().build();
    }
}
