package com.github.czsurvey.project.repository;

import com.github.czsurvey.project.entity.Survey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

/**
 * @author YanYu
 */
public interface SurveyRepository extends JpaRepository<Survey, Long> {

    boolean existsByIdAndUserId(Long id, Long userId);

    @Query(value = """
        select o1 from Survey o1
        left join Project o2 on o1.id = o2.ownerId and o2.ownerType = com.github.czsurvey.project.entity.enums.ProjectType.SURVEY
        where o2.deleted = false and o1.id = :id and o1.userId = :userId
    """)
    Optional<Survey> findEffectiveSurveyByIdAndUserId(Long id, Long userId);

    @Query(value = """
        select count(o1) > 0 from Survey o1
        left join Project o2 on o1.id = o2.ownerId and o2.ownerType = com.github.czsurvey.project.entity.enums.ProjectType.SURVEY
        where o2.deleted = false and o1.id = :id and o1.userId = :userId
    """)
    Boolean existsEffectiveSurveyByIdAndUserId(Long id, Long userId);
}
