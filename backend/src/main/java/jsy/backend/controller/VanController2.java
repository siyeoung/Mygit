package jsy.backend.controller;

import jakarta.servlet.http.HttpSession;
import jsy.backend.service.BankService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.text.DecimalFormat;

@Controller
public class VanController2 {
	private BankService bsvc;
	public VanController2(BankService bsvc) {
		this.bsvc = bsvc;
	}
	
	@GetMapping("/deposit")
	public String deposit(HttpSession se, Model mo) {
		mo.addAttribute("id", se.getAttribute("id"));
		return "deposit";
	}
	
	@PostMapping("/deposit")
	public String deposit(HttpSession se, Integer oman, Integer ilman, RedirectAttributes re) {
		if(oman == null && ilman == null) {
			re.addAttribute("msg", "지폐를 넣으신 후 입금 버튼을 클릭해 주세요 !");
			re.addAttribute("url", "back");
		}
		else {
			String id = (String)se.getAttribute("id");
			if(oman == null) oman = 0;
			if(ilman == null) ilman = 0;
			int money = oman*50000 + ilman*10000;
			bsvc.deposit(id,money);
			DecimalFormat d= new DecimalFormat("###,###");
			re.addAttribute("mag", d.format(money) + "원이 입금완료 되었습니다. (거래 내역 화면으로 이동)");
			re.addAttribute("url", "/myhistory");
		}
		return "redirect:/popup";
	}
	
	@GetMapping("/withdrawal")
	public String withdrawal(HttpSession se, Model mo){
		mo.addAttribute("id", se.getAttribute("id"));
		return "withdrawal";
	}
	
	@PostMapping("/withdrawal")
	public String withdrawal(HttpSession se, int money, RedirectAttributes re) {
		
		String id = (String)se.getAttribute("id");
		money *= 10000;
		bsvc.withdrawal(id, money);
		
		int oman = money/50000;
		int ilman = money%50000/10000;
		DecimalFormat d = new DecimalFormat("###,###");
		re.addAttribute("msg", d.format(money) + "원이 출금 완료 되었습니다 \n" + "(오만원권 " + oman + "장, 일만원권 " 
		+ ilman + "장) \n" + "거래 내역 화면으로 이동합니다.");
	re.addAttribute("url", "/myhistory");
	return "redirect:/popup";
	}
	
	@GetMapping("/myhistory")
	public String myhistory(HttpSession se, Model mo) {
		String id = (String)se.getAttribute("id");
		mo.addAttribute("id", id);
		
		String won = bsvc.mybalance(id);
		mo.addAttribute("won", won);
		
		String arr = bsvc.mybalance(id);
		mo.addAttribute("arr",arr);
		mo.addAttribute("arrsize", arr.lines());
		
		return "myhistory";
		
	}
	
	@GetMapping("/transfer")
	public String transfer(HttpSession se, Model mo) {
		mo.addAttribute("id", se.getAttribute("id"));
		return "transfer";
	}
	
	@PostMapping("/transfer")
	public String transfer(HttpSession se, int money, String tid, RedirectAttributes re) {
		String id = (String)se.getAttribute("id");
		if(id.equals(tid)) {
			re.addAttribute("msg", "고객님, 본인한테는 이체 불가합니다");
			re.addAttribute("url", "back");
		}
		else if(bsvc.transfer(id, money, tid)) {
			DecimalFormat d = new DecimalFormat("###,###");
			re.addAttribute("msg", tid + "님께 " + d.format(money) + "원이 이체 완료되었습니다. \n" + "(거래내역 화면으로 이동)");
		re.addAttribute("url", "/myhistory");
		}
		else {
			re.addAttribute("msg", "미등록 수신인 아이디 입니다. " + "수신인 아이디를 확인해 주세요.");
			re.addAttribute("url", "back");
		}
		return "redirect:/popup";
	}
}
