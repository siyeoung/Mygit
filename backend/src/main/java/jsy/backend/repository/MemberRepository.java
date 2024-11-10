package jsy.backend.repository;

import jakarta.transaction.Transactional;
import jsy.backend.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface MemberRepository extends JpaRepository<Member, String>{
	
	@Transactional
	@Modifying
	@Query("update Member set pw=?2, name=?3, phone=?4 where id=?1")  //소문자로 해도됨? : Member.java랑 같아야함.
	int updateMember(String id, String pw, String name, String phone);
	
	@Query("select count(id) from Member")
	long countMember();
	
	@Query("select sum(balance) from Member")
	long sumBalance();

	@Modifying
	@Query("update Member set balance=balance+?2 where id=?1")
	int updateBalance(String id, int money);
}
