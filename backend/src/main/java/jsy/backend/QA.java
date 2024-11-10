package jsy.backend;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
public class QA {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Integer no;
	public String id;
	@CreationTimestamp public LocalDateTime qdate;
	public String question;
	public LocalDateTime adate;
	public String answer;
}
