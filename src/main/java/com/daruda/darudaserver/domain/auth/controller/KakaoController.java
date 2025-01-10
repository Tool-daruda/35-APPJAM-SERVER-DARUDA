package com.daruda.darudaserver.domain.auth.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

@RequestMapping("/api/v1/users")
@RestController
@RequiredArgsConstructor
public class KakaoController {
    @Value("${KAKAO_CLIENT_ID")
    private String client_id;

    @Value("${kakao.redirect_uri")
    private String redirect_uri;

    @GetMapping("/kako/login-url")
    public RedirectView requestLogin(){
        RedirectView redirectView = new RedirectView();
        String location = "https://kauth.kakao.com/oauth/authorize?response_type=code&client_id="+client_id+"&redirect_uri="+redirect_uri;
        redirectView.setUrl(location);
        return redirectView;
    }

    @PostMapping()
}
