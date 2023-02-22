package com.github.czsurvey.project.repository;

import com.github.czsurvey.project.entity.Project;
import com.github.czsurvey.project.entity.enums.ProjectType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.List;
import java.util.Optional;

/**
 * @author YanYu
 */
public interface ProjectRepository extends JpaRepository<Project, Long>, QuerydslPredicateExecutor<Project> {


    Optional<Project> findByIdAndUserId(Long projectId, Long userId);

    List<Project> findByParentId(Long parentId);

    @Query(value = """
        select p from Project p
        where p.ownerType = com.github.czsurvey.project.entity.enums.ProjectType.FOLDER and p.userId = :userId
        and p.deleted = false
    """)
    List<Project> findEffectiveFolderByUserId(Long userId);

    @Query(value = """
        select p from Project p
        where p.ownerType = com.github.czsurvey.project.entity.enums.ProjectType.FOLDER and p.userId = :userId
    """)
    List<Project> findAllFolderByUserId(Long userId);

    @Query(value = """
        select
            p from Project p
        where
            p.ownerType = com.github.czsurvey.project.entity.enums.ProjectType.FOLDER
            and p.userId = :userId
            and p.id = :projectId
            and p.deleted = false
    """)
    Optional<Project> findEffectiveFolderByIdAndUserId(Long projectId, Long userId);

    @Query(value = """
        select
            p from Project p
        where
            p.ownerType = com.github.czsurvey.project.entity.enums.ProjectType.SURVEY
            and p.ownerId = :surveyId
            and p.deleted = false
    """)
    Optional<Project> findEffectiveProjectBySurveyId(Long surveyId);

    @Query(value = """
        select
            p from Project p
        where
            p.ownerType = com.github.czsurvey.project.entity.enums.ProjectType.SURVEY
            and p.ownerId = :surveyId
            and p.userId = :userId
            and p.deleted = false
    """)
    Optional<Project> findEffectiveProjectBySurveyIdAndUserId(Long surveyId, Long userId);

    Optional<Project> findByOwnerIdAndOwnerType(Long ownerId, ProjectType ownerType);
}
