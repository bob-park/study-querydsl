package study.querydsl.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Member;

import javax.persistence.EntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@Rollback(false)
class MemberJpaRepositoryTest {

  @Autowired private EntityManager em;

  @Autowired private MemberJpaRepository memberJpaRepository;

  @Test
  void testBasic() throws Exception {
    // given
    Member member1 = new Member("member1", 10);

    memberJpaRepository.save(member1);

    // when
    Member findMember = memberJpaRepository.findById(member1.getId()).get();

    List<Member> result1 = memberJpaRepository.findAll();

    List<Member> result2 = memberJpaRepository.findByUsername("member1");

    // then
    assertThat(findMember).isEqualTo(member1);
    assertThat(result1).containsExactly(member1);
    assertThat(result2).containsExactly(member1);
  }

  @Test
  void testBasicQuerydsl() throws Exception {
    // given
    Member member1 = new Member("member1", 10);

    memberJpaRepository.save(member1);

    // when

    Member findMember = memberJpaRepository.findById(member1.getId()).get();

    List<Member> result1 = memberJpaRepository.findAllQuerydsl();

    List<Member> result2 = memberJpaRepository.findByUsernameQuerydsl("member1");

    // then
    assertThat(findMember).isEqualTo(member1);
    assertThat(result1).containsExactly(member1);
    assertThat(result2).containsExactly(member1);
  }
}
