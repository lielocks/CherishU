package cherish.backend.member.service;

import cherish.backend.auth.jwt.JwtTokenProvider;
import cherish.backend.auth.jwt.TokenInfo;
import cherish.backend.common.service.RedisService;
import cherish.backend.member.constant.Constants;
import cherish.backend.member.dto.MemberFormDto;
import cherish.backend.member.dto.MemberInfoResponse;
import cherish.backend.member.email.service.EmailService;
import cherish.backend.member.email.util.EmailCodeGenerator;
import cherish.backend.member.model.Job;
import cherish.backend.member.model.Member;
import cherish.backend.member.repository.JobRepository;
import cherish.backend.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final RedisService redisService;
    private final JobRepository jobRepository;


    @Transactional
    public TokenInfo login(String email, String password) {
        // 1. Login ID/PW 를 기반으로 Authentication 객체 생성
        // 이때 authentication 는 인증 여부를 확인하는 authenticated 값이 false
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(email, password);
        // 2. 실제 검증 (사용자 비밀번호 체크)이 이루어지는 부분
        // authenticate 매서드가 실행될 때 CustomUserDetailsService 에서 만든 loadUserByUsername 메서드가 실행
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        // 3. 인증 정보를 기반으로 JWT 토큰 생성
        return jwtTokenProvider.generateToken(authentication);
    }

    @Transactional
    public Long join(MemberFormDto memberFormDto) {
        boolean isAlready = memberRepository.existsByEmail(memberFormDto.getEmail());
        if (isAlready) {
            throw new IllegalStateException(Constants.EMAIL_ALREADY);
        }
        // 회원가입 시 인증된 이메일인지 검증하는 로직 추가
        if (!isVerifiedEmail(memberFormDto.getEmail())) {
            throw new IllegalArgumentException(Constants.EMAIL_VERIFICATION_EXPIRED);
        }
        Member savedMember = memberRepository.save(Member.createMember(memberFormDto, passwordEncoder));
        return savedMember.getId();
    }

    @Transactional
    public void delete(String loginUserEmail) {
        Member member = memberRepository.findByEmail(loginUserEmail).orElseThrow(() -> new UsernameNotFoundException(Constants.MEMBER_NOT_FOUND));
        memberRepository.delete(member);
    }

    public boolean isMember(String email) {
        return memberRepository.existsByEmail(email);
    }

    @Transactional
    public void changePwd(String email, String pwd) {
        Member member = memberRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalStateException(Constants.MEMBER_NOT_FOUND));
        // 비밀번호 변경 시 인증된 이메일인지 검증하는 로직 추가
        if (!isVerifiedEmail(email)) {
            throw new IllegalArgumentException(Constants.EMAIL_VERIFICATION_EXPIRED);
        }
        member.changePwd(pwd, passwordEncoder);
    }

    public String sendEmailCode(String email) {
        if (redisService.hasEmailCodeKey(email)) {
            throw new IllegalStateException(Constants.TIME_LIMIT);
        }
        int dailyCount = redisService.getEmailCount(email);
        if (dailyCount >= 3) {
            throw new IllegalStateException(Constants.DAILY_COUNT_EXCEEDED);
        }
        if (isMember(email)) {
            throw new IllegalStateException(Constants.EMAIL_ALREADY);
        }
        String code = EmailCodeGenerator.generateCode();
        emailService.sendMessage(email, "인증번호 : " + code);
        setRedisCode(email, code);
        log.info("code {} has been sent to {}", code, email);
        return code;
    }

    private void setRedisCode(String email, String validCode) {
        int emailTimeLimit = 5 * 60;
        // redis에 인증번호 set
        redisService.setEmailCode(email, validCode, emailTimeLimit);
        // redis에 verified false로 set
        redisService.setEmailVerified(email, false, emailTimeLimit);
        // 하루 인증 횟수 1 증가
        redisService.incrementEmailCount(email);
    }

    public boolean validEmailCode(String email, String inputCode) {
        if (!redisService.hasEmailCodeKey(email))
            throw new IllegalStateException(Constants.EMAIL_CODE_NOT_FOUND);

        String validCode = redisService.getEmailCode(email);
        if (!inputCode.equals(validCode))
            throw new IllegalStateException(Constants.EMAIL_CODE_NOT_EQUAL);
        // redis에 인증 완료된 상태로 10분간 저장
        redisService.setEmailVerified(email, true, 10 * 60);
        return true;
    }

    @Transactional
    public Long changeInfo(String nickName, String jobName, String loginUserEmail) {
        Member member = memberRepository.findByEmail(loginUserEmail).orElseThrow(() -> new IllegalStateException(Constants.MEMBER_NOT_FOUND));
        Job job = jobRepository.findByName(jobName).orElseThrow(() -> new IllegalStateException("해당 직업이 존재하지 않습니다."));
        member.changeInfo(nickName, job);
        return member.getId();
    }

    public MemberInfoResponse getInfo(String loginUserEmail) {
        Member member = memberRepository.findByEmail(loginUserEmail).orElseThrow(() -> new IllegalStateException(Constants.MEMBER_NOT_FOUND));
        return MemberInfoResponse.of(member);

    }

    private boolean isVerifiedEmail(String email) {
        return redisService.hasVerifiedKey(email) && redisService.isEmailVerified(email);
    }

    public String setEmailCodeForPasswordReset(String email) {
        if (redisService.hasEmailCodeKey(email)) {
            throw new IllegalStateException(Constants.TIME_LIMIT);
        }
        int dailyCount = redisService.getEmailCount(email);
        if (dailyCount >= 3) {
            throw new IllegalStateException(Constants.DAILY_COUNT_EXCEEDED);
        }
        if (!isMember(email)) {
            throw new IllegalStateException(Constants.MEMBER_NOT_FOUND);
        }
        String code = EmailCodeGenerator.generateCode();
        emailService.sendMessage(email, "인증번호 : " + code);
        setRedisCode(email, code);
        log.info("code {} has been sent to {}", code, email);
        return code;
    }
}
