package com.howfun.controller;

import com.howfun.mapper.UserMapper;
import com.howfun.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpSession;
import java.util.Date;

/**
 * 用户管理
 */
@Controller
public class UserController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private HttpSession httpSession;

    @Autowired
    private JavaMailSender mailSender; //自动注入的Bean

    @Value("${spring.mail.username}")
    private String Sender; //读取配置文件中的参数

    /**
     * 登录跳转
     */
    @GetMapping("/user/login")
    public String loginGet() {
        return "login";
    }

    /**
     * 登录
     */
    @PostMapping("/user/login")
    public String loginPost(User user, Model model) {
        User user1 = userMapper.selectByNameAndPwd(user);

        if (user1 != null) {
            // 存入会话
            httpSession.setAttribute("user", user1);

            return "redirect:dashboard";
        } else {
            model.addAttribute("error", "用户名或密码错误，请重新登录！");

            return "login";
        }
    }

    /**
     * 注册
     */
    @GetMapping("/user/register")
    public String register() {
        return "register";
    }

    /**
     * 注册
     */
    @PostMapping("/user/register")
    public String registerPost(User user, Model model) {
        System.out.println("用户名" + user.getUserName());

        try {
            // 若查无此用户，抛出异常，注册用户
            userMapper.selectIsName(user);
            model.addAttribute("error", "该账号已存在！");
        } catch (Exception e) {
            Date date = new Date();

            user.setAddDate(date);
            user.setUpdateDate(date);

            userMapper.insert(user);

            System.out.println("注册成功");

            model.addAttribute("error", "恭喜您，注册成功！");

            return "login";
        }

        return "register";
    }

    /**
     * 登录跳转
     */
    @GetMapping("/user/forget")
    public String forgetGet() {
        return "forget";
    }

    /**
     * 忘记密码找回
     */
    @PostMapping("/user/forget")
    public String forgetPost(User user, Model model) {
        String password = userMapper.selectPasswordByName(user);

        if (password == null) {
            model.addAttribute("error", "帐号不存在或邮箱不正确！");
        } else {
            String email = user.getEmail();
            SimpleMailMessage message = new SimpleMailMessage();

            message.setFrom(Sender);
            message.setTo(email); //接收者邮箱
            message.setSubject("商家后台信息管理系统-密码找回");

            StringBuilder sb = new StringBuilder();
            sb.append(user.getUserName()).append("用户您好！您的注册密码是：").append(password).append("。感谢您使用商家信息管理系统！");

            message.setText(sb.toString());

            mailSender.send(message);

            model.addAttribute("error", "密码已发到您的邮箱,请查收！");
        }

        return "forget";
    }

    // 管理用户
    @GetMapping("/user/userManage")
    public String userManageGet(Model model) {
        // 获取当前登录的用户
        User user = (User) httpSession.getAttribute("user");

        User user1 = userMapper.selectByNameAndPwd(user);

        model.addAttribute("user", user1);

        return "user/userManage";
    }

    // 更改信息并提交，并更新当前会话的用户对象
    @PostMapping("/user/userManage")
    public String userManagePost(User user, HttpSession httpSession) {
        Date date = new Date();
        user.setUpdateDate(date);
        userMapper.update(user);

        httpSession.setAttribute("user", user);

        return "redirect:userManage";
    }
}
