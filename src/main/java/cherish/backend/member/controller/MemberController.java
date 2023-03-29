package cherish.backend.member.controller;

import cherish.backend.auth.security.SecurityUser;
import cherish.backend.member.dto.*;
import cherish.backend.member.dto.email.EmailCodeValidationRequest;
import cherish.backend.member.dto.email.EmailRequest;
import cherish.backend.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/member")
public class MemberController {
    private final MemberService memberService;
    // 회원 삭제
    @DeleteMapping("/delete")
    public ResponseEntity delete(@RequestParam("email") String email,@AuthenticationPrincipal SecurityUser securityUser){
        memberService.delete(email,securityUser.getMember().getEmail());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    // 회원 수정
    @PatchMapping("/change-info")
    public ResponseEntity changeInfo(@RequestBody ChangeInfoRequest request){
        Long memberId = memberService.changeInfo(request.getNickName(), request.getJobName(), request.getEmail());
        return ResponseEntity.status(HttpStatus.OK).body(memberId);
    }
    // 유틸 테스트
    // 객체로 받아오는 것
//    @GetMapping("/info")
//    public String info(@AuthenticationPrincipal SecurityUser member){
//        return member.getMember().getEmail();
//    }


}

