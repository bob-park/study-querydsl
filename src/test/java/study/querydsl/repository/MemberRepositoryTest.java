package study.querydsl.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.Team;

import javax.persistence.EntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static study.querydsl.entity.QMember.member;

@SpringBootTest
@Transactional
@Rollback(false)
class MemberRepositoryTest {

  @Autowired private EntityManager em;

  @Autowired private MemberRepository memberRepository;

  @Test
  void testBasic() throws Exception {
    // given
    Member member1 = new Member("member1", 10);

    memberRepository.save(member1);

    // when
    Member findMember = memberRepository.findById(member1.getId()).get();

    List<Member> result1 = memberRepository.findAll();

    List<Member> result2 = memberRepository.findByUsername("member1");

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

    condition.setAgeGoe(35);
    condition.setAgeLoe(45);
    condition.setTeamName("teamB");

    //    List<MemberTeamDto> result = memberJpaRepository.searchByBuilder(condition);
    List<MemberTeamDto> result = memberRepository.search(condition);

    // then

    assertThat(result).extracting("username").containsExactly("member4");
  }

  @Test
  void testSearchPageSimple() throws Exception {
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

    PageRequest pageRequest = PageRequest.of(0, 5);

    //    Page<MemberTeamDto> result = memberRepository.searchPageSimple(condition, pageRequest);
    Page<MemberTeamDto> result = memberRepository.searchPageComplex(condition, pageRequest);

    // then
    assertThat(result.getSize()).isEqualTo(3);
    assertThat(result.getContent())
        .extracting("username")
        .containsExactly("member1", "member2", "member3");
  }

  /**
   * QueryDSL Predicate Executor
   *
   * <pre>
   *     - Spring Data JPA repository 에서 바로 QueryDSL 조건을 정의하여 사용할 수 있다.
   *     - 간단한 entity 조회 정도는 가능하다.
   *     - 단, left outer join 이 필요한 경우 사용하지 못한다. - 실무에서 적합하지 않는다.
   *     - repository 계층이 아닌, 비지니스 로직에서 사용해야되는데, 그 부분에서 QueryDSL 에 의존해야 한다.
   * </pre>
   *
   * @throws Exception
   */
  @Test
  void testQuerydslPredicateExecutor() throws Exception {
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
    Iterable<Member> result =
        memberRepository.findAll(member.age.between(10, 40).and(member.username.eq("member1")));

    for (Member member : result) {
      System.out.println("member = " + member);
    }

    // then
  }
}
