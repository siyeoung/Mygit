package jsy.backend.controller;

import jakarta.servlet.http.HttpSession;
import jsy.backend.Member;
import jsy.backend.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.text.DecimalFormat;

@Controller
public class VanController1 {
	@Autowired
	private MemberRepository myrp;
	
	@GetMapping("/member/list")
	public String memberList(Model mo) {
		mo.addAttribute("arr", myrp.findAll());
		return "memberList";
	}
	
	@GetMapping("/login")
	public String login() {
		return "login";
	}
	
	@GetMapping("/member/register")
	public String memberRegister() {
		return "memberRegister";
	}
	
	@GetMapping("/popup")
	public String popup(String msg, String url, Model mo) {
		mo.addAttribute("msg",msg);
		mo.addAttribute("url",url);
		return "popup";
	}
	
	@GetMapping("/member/insert")
	public String memberInsert(String id, String pw, String name, String phone, RedirectAttributes re) {
		
		if(myrp.existsById(id)) {
			re.addAttribute("msg", id+"는 이미 사용되고 있는 아이디 입니다.");
			re.addAttribute("url","back");
		}
		else {
			Member me = new Member();
			me.id=id; me.pw=pw; me.name=name; me.phone=phone; me.balance=0;
			myrp.save(me);
			
			re.addAttribute("msg", id+"님, 반갑습니다! (로그인 화면으로 이동)");
		    re.addAttribute("url", "/login");
		}
		return "redirect:/popup";
	}
	
	@GetMapping("/login/check")
	public String loginCheck(HttpSession se, String id, Model mo, RedirectAttributes re) {
		if(myrp.existsById(id)) {
			se.setAttribute("id",id);
			return "redirect:/menu";
		}
		else {
			re.addAttribute("msg", id+"는 미등록 아이디입니다. 확인 후 로그인 해주시길 바랍니다.");
			re.addAttribute("url","/login");
			return "redirect:/popup";
		}
	}
	@GetMapping("/menu")
	public String menu(HttpSession se, Model mo) {
		mo.addAttribute("id", se.getAttribute("id"));
		return "menu";
	}
	
	@GetMapping("/myinfo")
	public String myinfo(HttpSession se, Model mo) {
		
		String id = (String)se.getAttribute("id");
		Member me = myrp.findById(id).get();
		
		mo.addAttribute("me", me);
		
		DecimalFormat fo = new DecimalFormat("###,###");
		mo.addAttribute("won", fo.format(me.balance)+" 원");
		
		return "myinfo";
		
	}
	
	@GetMapping("/myinfo/update")
	public String myinfoUpdate(HttpSession se, String pw, String name, String phone, RedirectAttributes re) {
		
		String id = (String)se.getAttribute("id");
		if(myrp.updateMember(id,pw,name,phone)==0)
			re.addAttribute("msg","정보 변경 실패. 고객센터로 문의하세요.");
		else
			re.addAttribute("msg", id+"님의 정보가 변경되었습니다.");
		
		re.addAttribute("url","back");
		return "redirect:/popup";
	}
	
	@GetMapping("/bankinfo")
	public String bankinfo(Model mo) {
		long a = myrp.countMember();
		long b = myrp.sumBalance();
		DecimalFormat fo = new DecimalFormat("###,###");
		mo.addAttribute("mcount", fo.format(a)+" 명");
		mo.addAttribute("bsum", fo.format(b)+" 원");
		return "bankinfo";
	}
	
	@GetMapping("/bankmap")
	public String bankmap() {
		return "bankmap";
	}
	
	@GetMapping("/logout")
	public String logout(HttpSession se, Model mo) {
		mo.addAttribute("id", se.getAttribute("id"));
		se.invalidate();
		return "logout";
	}
}
