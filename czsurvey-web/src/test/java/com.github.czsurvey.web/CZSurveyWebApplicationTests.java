//package com.github.czsurvey.web;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.github.czsurvey.common.util.PaginationUtil;
//import com.github.czsurvey.project.entity.Survey;
//import com.github.czsurvey.project.entity.enums.ProjectType;
//import com.github.czsurvey.project.response.ProjectResponse;
//import com.github.czsurvey.project.service.ProjectService;
//import com.github.czsurvey.project.service.question.context.SurveyContextService;
//import com.github.czsurvey.project.service.question.type.Radio;
//import com.querydsl.core.types.Projections;
//import com.querydsl.core.types.dsl.CaseBuilder;
//import com.querydsl.core.types.dsl.NumberExpression;
//import com.querydsl.jpa.impl.JPAQuery;
//import com.querydsl.jpa.impl.JPAQueryFactory;
//import jakarta.persistence.EntityManager;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Sort;
//
//
///**
// * @author YanYu
// */
//@SpringBootTest
//public class CZSurveyWebApplicationTests {
//
//    @Autowired
//    private ProjectService projectService;
//
//    @Autowired
//    private JPAQueryFactory jpaQueryFactory;
//
//    @Autowired
//    private EntityManager entityManager;
//
////    @Autowired
////    private QuestionTypeService questionTypeService;
//
//    @Autowired
//    private SurveyContextService surveyContextService;
//
//    @Autowired
//    private Radio radio;
//
//    @Test
//    public void testJpaQueryFactory() {
//
//        PageRequest request = PageRequest.of(0, 20, Sort.by(Sort.Order.by("id")));
//
//        NumberExpression<Integer> projectTypeExpression = new CaseBuilder()
//            .when(QProject.project.ownerType.eq(ProjectType.FOLDER))
//            .then(1)
//            .otherwise(2);
//
////        NumberExpression expression = (new NumberPath()).
//        JPAQuery<ProjectResponse> query = jpaQueryFactory.select(
//                Projections.constructor(ProjectResponse.class, QProject.project, QSurvey.survey.status)
//            )
//            .from(QProject.project)
//            .leftJoin(QSurvey.survey)
//            .on(QProject.project.ownerType.eq(ProjectType.SURVEY), QProject.project.ownerId.eq(QSurvey.survey.id))
//            .orderBy(projectTypeExpression.asc());
//
//        Page<ProjectResponse> page = PaginationUtil.page(query, request, Survey.class);
//        System.out.println(page.getContent());
//    }
//
//    @Test
//    public void testQuestionTypeService() throws JsonProcessingException {
//
//    }
//
//    @Test
//    public void testSurveyContext() {
////        SurveyContextHolder.setContext(1561272869399830528L);
////        System.out.println(surveyContextService.getQuestionMap());
////        System.out.println(QuestionTypeUtil.getQuestionTypes());
////        System.out.println(QuestionTypeUtil.getReferenceQuestionType());
//    }
//}
