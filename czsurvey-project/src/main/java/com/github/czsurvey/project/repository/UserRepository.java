package com.github.czsurvey.project.repository;

import com.github.czsurvey.project.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @author YanYu
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Query("Select u from User u where u.phone = :phoneOrEmail or u.email = :phoneOrEmail")
    Optional<User> findByPhoneOrEmail(@Param("phoneOrEmail") String phoneOrEmail);

    Optional<User> findTopByWxOpenid(String openId);
}
