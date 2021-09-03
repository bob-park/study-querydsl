package study.querydsl;

import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.QTeam;
import study.querydsl.entity.Team;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;

import java.util.List;

import static com.querydsl.jpa.JPAExpressions.select;
import static org.assertj.core.api.Assertions.assertThat;
import static study.querydsl.entity.QMember.member;
import static study.querydsl.entity.QTeam.team;

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

  @Test
  void testPagination() throws Exception {
    // given

    // when
    List<Member> result =
        queryFactory.selectFrom(member).orderBy(member.username.desc()).offset(1).limit(2).fetch();

    // then
    assertThat(result.size()).isEqualTo(2);
  }

  @Test
  void testPaginationTotalCount() throws Exception {
    // given

    // when

    // ! count 쿼리는 join 을 줄일 수 있기 때문에, 복잡한 쿼리는 별도로 count 를 생성하여 사용하자
    QueryResults<Member> fetchResults =
        queryFactory
            .selectFrom(member)
            .orderBy(member.username.desc())
            .offset(1)
            .limit(2)
            .fetchResults();

    // then
    assertThat(fetchResults.getTotal()).isEqualTo(4);
    assertThat(fetchResults.getLimit()).isEqualTo(2);
    assertThat(fetchResults.getOffset()).isEqualTo(1);
    assertThat(fetchResults.getResults().size()).isEqualTo(2);
  }

  @Test
  void testAggregation() throws Exception {
    // given

    // when
    List<Tuple> result =
        queryFactory
            .select(
                member.count(),
                member.age.sum(),
                member.age.avg(),
                member.age.max(),
                member.age.min())
            .from(member)
            .fetch();

    // then
    Tuple tuple = result.get(0);

    assertThat(tuple.get(member.count())).isEqualTo(4);
    assertThat(tuple.get(member.age.sum())).isEqualTo(100);
    assertThat(tuple.get(member.age.avg())).isEqualTo(25);
    assertThat(tuple.get(member.age.min())).isEqualTo(10);
    assertThat(tuple.get(member.age.max())).isEqualTo(40);
  }

  @Test
  void testGroupBy() throws Exception {
    // given

    // when
    // 팀의 이름과 각 팀의 평균 연령을 구하라
    List<Tuple> result =
        queryFactory
            .select(team.name, member.age.avg())
            .from(member)
            .join(member.team, team)
            .groupBy(team.name)
            //                .having()
            .fetch();

    Tuple teamA = result.get(0);
    Tuple teamB = result.get(1);

    // then
    assertThat(teamA.get(team.name)).isEqualTo("teamA");
    assertThat(teamA.get(member.age.avg())).isEqualTo(15);

    assertThat(teamB.get(team.name)).isEqualTo("teamB");
    assertThat(teamB.get(member.age.avg())).isEqualTo(35);
  }

  @Test
  void testBasicJoin() throws Exception {
    // given

    // when
    // teamA 에 소속된 모든 멤버
    List<Member> result =
        queryFactory
            .selectFrom(member)
            .join(member.team, team) // default : innerJoin, outerJoin (left, right) 모두 지원
            .where(team.name.eq("teamA"))
            .fetch();

    // then
    assertThat(result).extracting("username").containsExactly("member1", "member2");
  }

  /**
   * 연관관계가 없어도 join 가능 (Theta join)
   *
   * <p>! 주의할점
   *
   * <pre>
   *     - 외부 조인(outer join) 이 되지 않는다.
   *     - 하지만, on 을 이용하면 사용가능하다.
   * </pre>
   *
   * @throws Exception
   */
  @Test
  void testThetaJoin() throws Exception {
    // given
    em.persist(new Member("teamA"));
    em.persist(new Member("teamB"));
    em.persist(new Member("teamC"));

    em.flush();
    em.clear();

    // when
    // 회원이름과 팀이름이 같은 회원 조회

    List<Member> result =
        queryFactory.select(member).from(member, team).where(member.username.eq(team.name)).fetch();

    // then
    assertThat(result).extracting("username").containsExactly("teamA", "teamB");
  }

  @Test
  void testJoinFiltering() throws Exception {
    // given

    // when
    // 회원과 팀을 조인하면서, 팀 이름이 teamA 인 팀만 조인, 회원은 모두 조회
    // JPQL : select m from Member m left join m.team t on t.name='teamA'
    List<Tuple> result =
        queryFactory
            .select(member, team)
            .from(member)
            .leftJoin(member.team, team)
            .on(team.name.eq("teamA")) // ! 정말 외부조인이 필요할 경우에만 사용 권장
            .fetch();

    for (Tuple tuple : result) {
      System.out.println("tuple = " + tuple);
    }

    // then
  }

  @Test
  void testJoinOnNoRelation() throws Exception {
    // given
    em.persist(new Member("teamA"));
    em.persist(new Member("teamB"));
    em.persist(new Member("teamC"));

    em.flush();
    em.clear();

    // when
    // 연관관계가 없는 entity 외부 조인
    // 회원의 이름이 팀 이름과 같은 대상 외부 조인

    List<Tuple> result =
        queryFactory
            .select(member, team)
            .from(member)
            .leftJoin(team) // ! join 부분이 기존과 다른다. (entity 가 하나만 들어간다.)
            .on(member.username.eq(team.name)) // id match 부분이 빠져버린다.
            .fetch();

    // then
    for (Tuple tuple : result) {
      System.out.println("tuple = " + tuple);
    }
  }

  @PersistenceUnit private EntityManagerFactory emf;

  @Test
  void testNoFetchJoin() throws Exception {
    // given

    em.flush();
    em.clear();

    // when
    Member findMember =
        queryFactory.selectFrom(member).where(member.username.eq("member1")).fetchOne();

    boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());

    // then
    assertThat(loaded).isFalse();
  }

  @Test
  void testFetchJoin() throws Exception {
    // given

    em.flush();
    em.clear();

    // when
    Member findMember =
        queryFactory
            .selectFrom(member)
            .join(member.team, team)
            .fetchJoin()
            .where(member.username.eq("member1"))
            .fetchOne();

    boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());

    // then
    assertThat(loaded).isTrue();
  }

  /**
   * Sub Query
   *
   * <p>! 주의해야할 점 (JPA 와 동일)
   *
   * <pre>
   *     - from 절에서 sub query 가 안됨 - JPA 가 지원해주지 않음
   *        1. join 으로 변경하여 해결한다.
   *        2. query 를 두번 실행한다.
   *        3. native query 를 사용한다. (JdbcTemplate, Mybatis, ...)
   * </pre>
   *
   * @throws Exception
   */
  @Test
  void testSubQuery() throws Exception {
    // given

    // when

    QMember subMember = new QMember("sub_member");

    // 나이가 가장 많은 회원 조회
    List<Member> result =
        queryFactory
            .selectFrom(member)
            .where(member.age.eq(select(subMember.age.max()).from(subMember)))
            .fetch();

    // then

    assertThat(result.get(0).getAge()).isEqualTo(40);
  }

  @Test
  void testSubQuery2() throws Exception {
    // given

    // when

    QMember subMember = new QMember("sub_member");

    // 나이가 평균 이상인 회원
    List<Member> result =
        queryFactory
            .selectFrom(member)
            .where(member.age.goe(select(subMember.age.avg()).from(subMember)))
            .fetch();

    // then

    assertThat(result).extracting("age").containsExactly(30, 40);
  }

  @Test
  void testSubQuery3() throws Exception {
    // given

    // when

    QMember subMember = new QMember("sub_member");

    // in
    List<Member> result =
        queryFactory
            .selectFrom(member)
            .where(member.age.in(select(subMember.age).from(subMember).where(subMember.age.gt(10))))
            .fetch();

    // then

    assertThat(result).extracting("age").containsExactly(20, 30, 40);
  }

  @Test
  void testSubQuery4() throws Exception {
    // given
    QMember subMember = new QMember("sub_member");

    // when
    // select sub query
    List<Tuple> result =
        queryFactory
            .select(member.username, select(subMember.age.avg()).from(subMember))
            .from(member)
            .fetch();

    // then
    for (Tuple tuple : result) {
      System.out.println("tuple = " + tuple);
    }
  }

  /**
   * case
   *
   * <pre>
   *     - 가급적 사용하지 말자
   *     - DB 는 데이터를 조건에 맞게 줄이거나 데이터를 가져오는 것, 데이터를 가공하는 것이 아님
   * </pre>
   *
   * @throws Exception
   */
  @Test
  void testBasicCase() throws Exception {
    // given

    // when
    List<String> result =
        queryFactory
            .select(member.age.when(10).then("열살").when(20).then("스무살").otherwise("기타"))
            .from(member)
            .fetch();

    // then
    for (String s : result) {
      System.out.println("s = " + s);
    }
  }

  @Test
  void testComplexCase() throws Exception {
    // given

    // when

    List<String> result =
        queryFactory
            .select(
                new CaseBuilder()
                    .when(member.age.between(0, 20))
                    .then("0~20살")
                    .when(member.age.between(21, 30))
                    .then("21~30살")
                    .otherwise("기타"))
            .from(member)
            .fetch();

    // then
    for (String s : result) {
      System.out.println("s = " + s);
    }
  }
}
