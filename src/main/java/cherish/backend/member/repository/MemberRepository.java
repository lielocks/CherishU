package cherish.backend.member.repository;

import cherish.backend.member.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);
    boolean existsByEmail(String email);

    Optional<Member> findById(Long id);

    @Query(value = "select m from Member m left join fetch m.itemJob where m.id = :id")
    Member findMemberById(@Param("id") Long id);
}