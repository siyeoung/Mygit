package jsy.backend.repository;

import jsy.backend.Bank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BankRepository extends JpaRepository<Bank, Integer> {
	@Modifying
	@Query("update Member set balance=balance+?2 where id=?1")
	int updateBalance(String id, int money);
	
	List<Bank> findByIdOrderByTdateDesc(String id);
}
