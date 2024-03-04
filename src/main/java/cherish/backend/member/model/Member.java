package cherish.backend.member.model;

import cherish.backend.common.model.BaseTimeEntity;
import cherish.backend.item.model.ItemJob;
import cherish.backend.member.dto.MemberFormDto;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@ToString
public class Member extends BaseTimeEntity {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;
    private String name; // 이름
    private String nickname; // 닉네임
    private String email; // 이메일
    private String password; // 패스워드
    private boolean informationCheck; // 광고성 동의
    // 추가 정보
    @Enumerated(EnumType.STRING)
    private Gender gender; // 성별
    private LocalDate birth; // 생일

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id")
    private ItemJob itemJob;

    // 생성 메소드
    @Enumerated(EnumType.STRING)
    private Role roles;

    public enum Gender {
        MALE, FEMALE, NONE
    }

    public enum Role {
        ROLE_USER, ROLE_ADMIN
    }

    public static Member createMember(MemberFormDto formDto, PasswordEncoder passwordEncoder){
        Gender _gender;
        try {
            _gender = Gender.valueOf(formDto.getGender());
        } catch (IllegalArgumentException | NullPointerException e) {
            _gender = Gender.NONE;
        }

        return Member.builder()
                .name(formDto.getName())
                .nickname(formDto.getNickname())
                .email(formDto.getEmail())
                .password(passwordEncoder.encode(formDto.getPassword()))
                .informationCheck(formDto.isInfoCheck())
                .gender(_gender)
                .birth(formDto.getBirth())
                .roles(Role.ROLE_USER)
                .build();
    }

    // 유틸 메소드
    // 비밀번호 변경
    public void changePwd(String password) {
        this.password = password;
    }

    public void changeInfo(String nickname, ItemJob itemJob){
        this.nickname = nickname;
        this.itemJob = itemJob;
    }
}
