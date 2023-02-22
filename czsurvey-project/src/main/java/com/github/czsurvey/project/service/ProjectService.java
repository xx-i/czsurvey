package com.github.czsurvey.project.service;

import cn.hutool.core.util.StrUtil;
import com.github.czsurvey.common.exception.BadRequestAlertException;
import com.github.czsurvey.common.exception.InvalidParameterException;
import com.github.czsurvey.common.util.PaginationUtil;
import com.github.czsurvey.extra.security.model.LoginUser;
import com.github.czsurvey.project.constant.ProjectConstant;
import com.github.czsurvey.project.entity.Project;
import com.github.czsurvey.project.entity.QProject;
import com.github.czsurvey.project.entity.QSurvey;
import com.github.czsurvey.project.entity.enums.ProjectType;
import com.github.czsurvey.project.repository.*;
import com.github.czsurvey.project.request.MoveProjectRequest;
import com.github.czsurvey.project.request.ProjectRequest;
import com.github.czsurvey.project.response.ProjectResponse;
import com.github.czsurvey.project.service.question.QuestionTypeUtil;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * @author YanYu
 */
@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;

    private final SurveyAnswerRepository surveyAnswerRepository;

    private final SurveyQuestionRepository surveyQuestionRepository;

    private final SurveySettingRepository surveySettingRepository;

    private final SurveyRepository surveyRepository;

    private final JPAQueryFactory jpaQueryFactory;

    /**
     * 创建一个文件夹
     * @param folderName 文件夹名
     */
    public Project createFolder(String folderName) {
        Project project = new Project();
        project.setName(folderName);
        project.setOwnerType(ProjectType.FOLDER);
        project.setParentId(ProjectConstant.ROOT_PARENT_ID);
        project.setUserId(LoginUser.me().getId());
        project.setDeleted(false);
        return projectRepository.save(project);
    }

    public List<Project> listMyFolder() {
        return projectRepository.findEffectiveFolderByUserId(LoginUser.me().getId());
    }

    public List<Project> listMyAllFolder() {
        return projectRepository.findAllFolderByUserId(LoginUser.me().getId());
    }

    public Page<ProjectResponse> pageMyProject(ProjectRequest projectRequest, Pageable pageRequest) {
        QProject qProject = QProject.project;
        QSurvey qSurvey = QSurvey.survey;

        BooleanBuilder booleanBuilder = new BooleanBuilder();
        booleanBuilder.and(qProject.userId.eq(LoginUser.me().getId()));

        if (projectRequest.getTrash() != null) {
            booleanBuilder.and(qProject.deleted.eq(projectRequest.getTrash()));
        } else {
            booleanBuilder.and(qProject.deleted.eq(false));
        }

        if (StrUtil.isBlank(projectRequest.getName()) && projectRequest.getStatus() == null && projectRequest.getFolderId() == null) {
            booleanBuilder.and(qProject.parentId.eq(ProjectConstant.ROOT_PARENT_ID));
        }
        if (StrUtil.isNotBlank(projectRequest.getName())) {
            booleanBuilder.and(qProject.name.like("%" + projectRequest.getName() + "%"));
        }
        if (projectRequest.getStatus() != null) {
            booleanBuilder.and(qProject.ownerType.ne(ProjectType.FOLDER))
                .and(qSurvey.status.eq(projectRequest.getStatus()));
        }
        if (projectRequest.getFolderId() != null) {
            booleanBuilder.and(qProject.parentId.eq(projectRequest.getFolderId()));
        }
        NumberExpression<Integer> orderExpression = new CaseBuilder()
            .when(QProject.project.ownerType.eq(ProjectType.FOLDER))
            .then(1)
            .otherwise(2);

        JPAQuery<ProjectResponse> query = jpaQueryFactory.select(
                Projections.constructor(ProjectResponse.class, qProject, qSurvey.status)
            )
            .from(qProject)
            .leftJoin(qSurvey)
            .on(qProject.ownerType.eq(ProjectType.SURVEY).and(qProject.ownerId.eq(qSurvey.id)))
            .where(booleanBuilder.getValue())
            .orderBy(orderExpression.asc());

        return PaginationUtil.page(query, pageRequest, Project.class, projectResponse -> {
            Project project = projectResponse.getProject();
            if (ProjectType.SURVEY.equals(project.getOwnerType())) {
                Long total = surveyAnswerRepository.countBySurveyId(project.getOwnerId());
                Long countLast30Days = surveyAnswerRepository.countBySurveyIdAndEndedAtAfter(project.getOwnerId(), LocalDateTime.now().plusDays(-30));
                Long questionCount = surveyQuestionRepository.countBySurveyIdAndTypeNotIn(project.getOwnerId(), QuestionTypeUtil.getDisplayModeQuestionTypes());
                projectResponse.setQuantityCollected(total);
                projectResponse.setQuantityCollectedLast30Days(countLast30Days);
                projectResponse.setQuestionCount(questionCount);
                return projectResponse;
            }
            return projectResponse;
        });
    }

    public Project renameFolder(Long folderId, String folderName) {
        Project project = projectRepository.findEffectiveFolderByIdAndUserId(folderId, LoginUser.me().getId())
            .orElseThrow(() -> new BadRequestAlertException("文件夹不存在", Project.entityName(), "folder_not_found"));
        project.setName(folderName);
        return projectRepository.save(project);
    }

    @Transactional(rollbackFor = Exception.class)
    public void moveToTrash(Long projectId) {
        Project project = projectRepository.findByIdAndUserId(projectId, LoginUser.me().getId())
            .orElseThrow(() -> new BadRequestAlertException("项目不存在", Project.entityName(), "project_not_found"));
        if (project.getOwnerType().equals(ProjectType.FOLDER)) {
            List<Project> subProject = projectRepository.findByParentId(project.getId());
            subProject.forEach(e -> e.setDeleted(true));
            projectRepository.saveAll(subProject);
        }
        project.setDeleted(true);
        project.setParentId(ProjectConstant.ROOT_PARENT_ID);
        projectRepository.save(project);
    }

    @Transactional(rollbackFor = Exception.class)
    public void moveProject(MoveProjectRequest moveProjectRequest) {
        if (!moveProjectRequest.getTo().equals(ProjectConstant.ROOT_PARENT_ID)) {
            projectRepository.findEffectiveFolderByIdAndUserId(moveProjectRequest.getTo(), LoginUser.me().getId())
                .orElseThrow(() -> new InvalidParameterException("文件夹ID: " + moveProjectRequest.getTo() + "不存在", Project.entityName()));
        }
        Project project = projectRepository.findByIdAndUserId(moveProjectRequest.getProjectId(), LoginUser.me().getId())
            .orElseThrow(() -> new InvalidParameterException("被移动的项目不存在，项目ID: " + moveProjectRequest.getProjectId(), Project.entityName()));
        if (project.getOwnerType().equals(ProjectType.FOLDER)) {
            throw new InvalidParameterException("不能移动文件夹", Project.entityName());
        }
        project.setParentId(moveProjectRequest.getTo());
        projectRepository.save(project);
    }

    @Transactional(rollbackFor = Exception.class)
    public void recoverProject(Long projectId) {
        Project project = projectRepository.findByIdAndUserId(projectId, LoginUser.me().getId())
            .orElseThrow(() -> new InvalidParameterException("项目ID: " + projectId + " 不存在", Project.entityName()));
        if (ProjectType.FOLDER.equals(project.getOwnerType())) {
            List<Project> projects = projectRepository.findByParentId(projectId)
                .stream()
                .peek(item -> item.setDeleted(false))
                .toList();
            projectRepository.saveAll(projects);
        } else {
            project.setParentId(ProjectConstant.ROOT_PARENT_ID);
        }
        project.setDeleted(false);
        projectRepository.save(project);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteProject(Long projectId) {
        Project project = projectRepository.findByIdAndUserId(projectId, LoginUser.me().getId())
            .orElseThrow(() -> new InvalidParameterException("项目ID: " + projectId + " 不存在", Project.entityName()));
        List<Long> surveyIds;
        if (ProjectType.FOLDER.equals(project.getOwnerType())) {
            surveyIds = projectRepository.findByParentId(projectId)
                .stream()
                .map(Project::getOwnerId)
                .toList();
        } else {
            surveyIds = Collections.singletonList(project.getOwnerId());
        }
        if (surveyIds.size() > 0) {
            surveyRepository.deleteAllById(surveyIds);
            surveySettingRepository.deleteAllById(surveyIds);
            surveyQuestionRepository.deleteAllBySurveyIdIn(surveyIds);
            surveyAnswerRepository.deleteAllBySurveyIdIn(surveyIds);
        }
        projectRepository.delete(project);
    }
}
