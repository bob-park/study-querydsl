package study.querydsl;

import com.querydsl.core.QueryResults;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Member;
import study.querydsl.entity.Team;

import javax.persistence.EntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static study.querydsl.entity.QMember.member;

@SpringBootTest
@Transactional
@Rollback(false)
class QuerydslBasicTest {

  @Autowired private EntityManager em;

  private JPAQueryFactory queryFactory;

  @BeforeEach
  void setup() {
    queryFactory = new JPAQueryFactory(em);

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
  }

  @Test
  void startJPQL() throws Exception {
    // given

    // when
    String qlString = "select m from Member m where m.username = :username";

    Member findMember =
        em.createQuery(qlString, Member.class)
            .setParameter("username", "member1")
            .getSingleResult();

    // then
    assertThat(findMember.getUsername()).isEqualTo("member1");
  }

  @Test
  void startQuerydsl() throws Exception {
    // given

    // when
    Member findMember =
        queryFactory.select(member).from(member).where(member.username.eq("member1")).fetchOne();

    // then
    assertThat(findMember.getUsername()).isEqualTo("member1");
  }

  @Test
  void testSearchParam() throws Exception {
    // given

    // when
    Member findMember =
        queryFactory
            .selectFrom(member)
            .where(
                // member.username.eq("member1").and(member.age.eq(10)) // and, or 로
                // chaining 할 수 있다.
                member.username.eq("member1"), member.age.eq(10)
                // parameter 로 할 수 있다. 단, and 조건이 된다.
                // null 은 무시된다.
                )
            .fetchOne();

    // then
    assertThat(findMember.getUsername()).isEqualTo("member1");
  }

  @Test
  void testResultFetch() throws Exception {
    // given

    // when
    List<Member> fetch = queryFactory.selectFrom(member).fetch(); // list 조회
    //    queryFactory.selectFrom(member).fetchOne(); // 단건 조회
    queryFactory.selectFrom(member).fetchFirst(); // limit 1, 단건 조회
    QueryResults<Member> fetchResults =
        queryFactory.selectFrom(member).fetchResults(); // total, count, 등이 있다.

    fetchResults.getTotal(); // total count
    List<Member> results = fetchResults.getResults(); // contents

    queryFactory.selectFrom(member).fetchCount(); // count 만 가져온다. deprecated

    // then

  }

  @Test
  void testSort() throws Exception {
    // given
    // 1. 회원 나이 내림차순
    // 2. 회원 이름 올림차순
    // 단, 2에서 회원 이름이 없을 경우 마지막에 출력(nulls last)
    em.persist(new Member(null, 100));
    em.persist(new Member("member5", 100));
    em.persist(new Member("member6", 100));

    em.flush();
    em.clear();

    // when
    List<Member> result =
        queryFactory
            .selectFrom(member)
            .where(member.age.eq(100))
            .orderBy(member.age.desc(), member.username.asc().nullsLast())
            .fetch();

    Member member5 = result.get(0);
    Member member6 = result.get(1);
    Member memberNull = result.get(2);

    // then

    assertThat(member5.getUsername()).isEqualTo("member5");
    assertThat(member6.getUsername()).isEqualTo("member6");
    assertThat(memberNull.getUsername()).isNull();
  }
}
