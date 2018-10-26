package cn.captainshen.controller;

import cn.captainshen.activiti.ActivitiUtil;
import org.activiti.engine.*;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import javax.servlet.http.HttpSession;

@Controller
public class LoginController {
    @RequestMapping(value = {"/", "/login"}, method = RequestMethod.GET)
    public String login() {
        return "login";
    }

    @RequestMapping(value = "/processLogin", method = RequestMethod.POST)
    public String processLogin(@RequestParam("username") String userId,
                               @RequestParam("password") String password,
                               HttpSession httpSession,
                               RedirectAttributes redirectAttributes){
        IdentityService identityService = ActivitiUtil.getIdentityService();
        if(identityService.checkPassword(userId, password)){
            httpSession.setAttribute("loginUser", userId);
            Group group = identityService.createGroupQuery().groupMember(userId).singleResult();
            httpSession.setAttribute("userGroup", group.getId());
            return "redirect:/index";
        }else{
            redirectAttributes.addFlashAttribute("error_msg", "用户名或密码错误");
            return "redirect:/login";
        }
    }

    @RequestMapping(value = "/quitLogin", method = RequestMethod.GET)
    public String quitLogin(HttpSession httpSession){
        httpSession.invalidate();
        return "redirect:/login";
    }
}