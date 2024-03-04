//package cherish.backend.member.service;
//
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//
//import static org.junit.jupiter.api.Assertions.assertThrows;
//
//@SpringBootTest
//class MemberServiceTest {
//    @Autowired
//    MemberService memberService;
//
//    @Test
//    public void 이메일_인증_불일치_확인(){
//        //given
//        String email = "cherishu.fullstack@gmail.com";
//        //when
//        memberService.sendEmailCode(email);
//        assertThrows(IllegalStateException.class,()->memberService.validEmailCode(email, "test"));
//    }
//
//    @Test
//    public void 이메일_30초내에_재발송(){
//        //given
//        String email = "cherishu.fullstack@gmail.com";
//        //when
//        memberService.sendEmailCode(email);
//        assertThrows(IllegalStateException.class,()->memberService.sendEmailCode(email) );
//        //then
//    }
//
//
//}