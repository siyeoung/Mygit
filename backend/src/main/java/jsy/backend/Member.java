package jsy.backend;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
public class Member {
	@Id public String id;
	public String pw;
	public String name;
	public String phone;
	public Integer balance;
	@CreationTimestamp public LocalDateTime rdate;
}
