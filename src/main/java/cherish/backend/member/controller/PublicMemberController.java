package cherish.backend.member.controller;

import cherish.backend.auth.jwt.config.JwtConfig;
import cherish.backend.auth.jwt.dto.TokenResponseDto;
import cherish.backend.member.dto.ChangePwdRequest;
import cherish.backend.member.dto.MemberFormDto;
import cherish.backend.member.dto.MemberLoginRequestDto;
import cherish.backend.member.dto.email.EmailCodeValidationRequest;
import cherish.backend.member.dto.email.EmailRequest;
import cherish.backend.member.service.MemberService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.server.Cookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.*;

import static cherish.backend.common.constant.CommonConstants.REFRESH_TOKEN_COOKIE_NAME;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/public/member")
public class PublicMemberController {

    private final MemberService memberService;
    private final JwtConfig jwtConfig;

    //회원 회원가입
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/register")
    public Long register(@RequestBody @Valid MemberFormDto memberFormDto){
        return memberService.join(memberFormDto);
    }

    // 회원 로그인
    @PostMapping("/login")
    public TokenResponseDto login(@RequestBody @Valid MemberLoginRequestDto memberLoginRequestDto, HttpServletResponse response) {
        var token = memberService.login(memberLoginRequestDto.getEmail(), memberLoginRequestDto.getPassword());
        log.info("member login = {}", memberLoginRequestDto.getEmail());
        ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, token.getRefreshToken())
                .maxAge(jwtConfig.getRefreshTokenExpireSeconds())
                .path("/")
                .sameSite(Cookie.SameSite.NONE.attributeValue())
                .secure(true)
                .httpOnly(true)
                .build();
        response.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return token.toResponseDto();
    }
    // 비밀번호 수정
    // 기존비밀번호 확인이 체크가 되어있거나
    // 혹은 현재 로그인한 사용자가 바꾸길 원하는 이메일과 같은 경우
    @PostMapping("/change-password")
    public void changePwd(@RequestBody @Valid ChangePwdRequest request){
        memberService.changePwd(request.getEmail(), request.getPassword());
    }
    // 회원가입 시 코드 발송
//    @PostMapping("/register/code")
//    public void sendEmailCodeForRegistration(@RequestBody @Valid EmailRequest emailRequest){
//        memberService.sendEmailCode(emailRequest.getEmail());
//    }

    // 비밀번호 재설정 시 코드 발송
//    @PostMapping("/change-password/code")
//    public void sendEmailCodeForPasswordReset(@RequestBody @Valid EmailRequest emailRequest) {
//        memberService.setEmailCodeForPasswordReset(emailRequest.getEmail());
//    }
    // 이메일 코드 검증
    @PostMapping("/code-valid")
    public void validEmailCode(@RequestBody @Valid EmailCodeValidationRequest request){
        memberService.validEmailCode(request.getEmail(), request.getCode());
    }
}