package study.querydsl;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.Team;

import javax.persistence.EntityManager;

import java.util.List;

import static study.querydsl.entity.QMember.member;

@SpringBootTest
@Transactional
@Rollback(false)
class QuerydslIntermediateTest {

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

  /**
   * basic projections
   *
   * @throws Exception
   */
  @Test
  void testSimpleProjections() throws Exception {
    // given

    // when
    List<String> result = queryFactory.select(member.username).from(member).fetch();

    for (String s : result) {
      System.out.println("s = " + s);
    }

    // then
  }

  /**
   * Projection Tuple
   *
   * <pre>
   *    - tuple 를 repository 계층에서만 필요할 때 쓰는 것이 좋다.
   * </pre>
   *
   * @throws Exception
   */
  @Test
  void testTupleProjections() throws Exception {
    // given

    // when

    List<Tuple> result = queryFactory.select(member.username, member.age).from(member).fetch();

    for (Tuple tuple : result) {
      String username = tuple.get(member.username);
      Integer age = tuple.get(member.age);

      System.out.println("username = " + username);
      System.out.println("age = " + age);
    }

    // then

  }
}
