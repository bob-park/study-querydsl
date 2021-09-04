package study.querydsl.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.Team;

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

  @Test
  void testSearch() throws Exception {
    // given
    Team teamA = new Team("teamA");
    Team teamB = new Team("teamB");

    em.persist(teamA);
    em.persist(teamB);

    Member member1 = new Member("member1", 10, teamA);
    Member member2 = new Member("member2", 20, teamA);
    Member member3 = new Member("member3", 30, teamB);
    Member member4 = new Member("member4", 40, teamB);

    em.persist(member1);
    em.persist(member2);
    em.persist(member3);
    em.persist(member4);

    // when
    MemberSearchCondition condition = new MemberSearchCondition();

    //    condition.setAgeGoe(35);
    //    condition.setAgeLoe(45);
    //    condition.setTeamName("teamB");

    List<MemberTeamDto> result = memberJpaRepository.searchByBuilder(condition);

    // then

    assertThat(result).extracting("username").containsExactly("member3", "member4");
  }
}
