package jsy.backend.service;

import jakarta.transaction.Transactional;
import jsy.backend.Bank;
import jsy.backend.repository.BankRepository;
import jsy.backend.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.List;

@Service
public class BankService {
	
	@Autowired
	private MemberRepository myrp;
	@Autowired
	private BankRepository brep;
	
	@Transactional
	public void deposit(String id, int money) {
		Bank obj = new Bank();
		obj.id = id; obj.de = money; obj.wi = 0; obj.tcode = 0;
		brep.save(obj);
		myrp.updateBalance(id, money);
		}
	
	@Transactional
	public void withdrawal(String id, int money) {
		Bank obj = new Bank();
		brep.save(obj);
		myrp.updateBalance(id, -money);
	}
	
	public String mybalance(String id) {
		int balance = myrp.findById(id).get().balance;
		DecimalFormat d = new DecimalFormat("###,###");
		return d.format(balance) + " 원";
	}
	
	public List<Bank> myhistory(String id){
		return brep.findByIdOrderByTdateDesc(id);
	}
	
	@Transactional
	public boolean transfer(String id, int money, String tid) {
		if(!myrp.existsById(tid)) return false;
		
		Bank a = new Bank();
		a.id = id;
		a.de = 0;
		a.wi = money;
		a.tcode = 1;
		a.tid = tid;
		brep.save(a);
		myrp.updateBalance(id, -money);
		
		Bank b = new Bank();
		b.id = tid;
		b.de = money;
		b.wi = 0;
		b.tcode = 2;
		b.tid = id;
		brep.save(b);
		myrp.updateBalance(tid, money);
		
		return true;
	}
}
