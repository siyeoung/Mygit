package jsy.backend.repository;

import jakarta.transaction.Transactional;
import jsy.backend.QA;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface QARepository extends JpaRepository<QA, Integer>{

    @Transactional
    @Modifying
    @Query("update QA set adate=now(), answer=?2 where no=?1")
    int updateAnswer(Integer no, String answer);
}
