package jsy.backend.controller;

import jakarta.servlet.http.HttpSession;
import jsy.backend.QA;
import jsy.backend.repository.QARepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class VanController3 {
	
	@Autowired
	private QARepository qrep;

	@GetMapping("/qa/question")
	public String question(HttpSession se, Model mo){
		String id = (String)se.getAttribute("id");
		mo.addAttribute("id",id);
		mo.addAttribute("arr",qrep.findAll());
		return "question";
	}

	@PostMapping("/qa/question")
	public String question(HttpSession se, String question, Model mo, RedirectAttributes re){
		String id = (String)se.getAttribute("id");
		QA qa = new QA();
		qa.id = id;
		qa.question = question;
		qa.answer = "(답변준비중)";
		qrep.save(qa);

		re.addAttribute("msg", id + " 님의 질문이 정상적으로 등록되었습니다.");
		re.addAttribute("url", "/qa/question");
		return "redirect:/popup";
	}

	@GetMapping("/qa/answer")
	public String answer(HttpSession se, Model mo){
		mo.addAttribute("id",se.getAttribute("id"));
		mo.addAttribute("arr",qrep.findAll());
		return "answer";
	}

	@PostMapping("/qa/answer")
	public String answer(Integer no, String answer, Model mo, RedirectAttributes re){
		if (!qrep.existsById(no)) {
			re.addAttribute("msg", no + "번은 존재하지 않는 질문번호입니다.");
			re.addAttribute("url","back");
		}
		else {
			qrep.updateAnswer(no,answer);
			re.addAttribute("msg", "답변이 등록되었습니다.");
			re.addAttribute("url", "/qa/answer");
		}
		return "redirect:/popup";
	}
}
