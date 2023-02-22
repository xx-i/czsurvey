package com.github.czsurvey.project.entity;

import com.github.czsurvey.project.entity.audit.DateAudit;
import com.github.czsurvey.project.entity.enums.AnswererType;
import com.github.czsurvey.project.entity.enums.LimitFreq;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * @author YanYu
 */
@Getter
@Setter
@ToString
@Entity
@Table(name = "t_survey_setting")
@AllArgsConstructor
@NoArgsConstructor
public class SurveySetting extends DateAudit  {

    @Id
    private Long surveyId;

    @Column(name = "is_display_question_no")
    private Boolean displayQuestionNo;

    @Column(name = "is_allow_rollback")
    private Boolean allowRollback;

    @Column(name = "is_login_required")
    private Boolean loginRequired;

    @Enumerated(EnumType.STRING)
    private AnswererType answererType;

    @Column(name = "is_enable_user_answer_limit")
    private Boolean enableUserAnswerLimit;

    @Enumerated(EnumType.STRING)
    private LimitFreq userLimitFreq;

    private Integer userLimitNum;

    @Column(name = "is_enable_ip_answer_limit")
    private Boolean enableIpAnswerLimit;

    @Enumerated(EnumType.STRING)
    private LimitFreq ipLimitFreq;

    private Integer ipLimitNum;

    private LocalDateTime beginTime;

    private LocalDateTime endTime;

    private Integer maxAnswers;

    @Column(name = "is_add_to_contact")
    private Boolean addToContact;

    private Long contactGroupId;

    @Column(name = "is_enable_change")
    private Boolean enableChange;

    @Column(name = "is_anonymously")
    private Boolean anonymously;

    @Column(name = "is_breakpoint_resume")
    private Boolean breakpointResume;

    @Column(name = "is_save_last_answer")
    private Boolean saveLastAnswer;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SurveySetting that)) return false;
        return Objects.equals(surveyId, that.surveyId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(surveyId);
    }

    public static String entityName() {
        return "surveySetting";
    }
}
