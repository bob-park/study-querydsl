package study.querydsl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.dto.MemberDto;
import study.querydsl.dto.QMemberDto;
import study.querydsl.dto.UserDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.Team;

import javax.persistence.EntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
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
   *    - tuple ??? repository ??????????????? ????????? ??? ?????? ?????? ??????.
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

  /**
   * ?????? JPA ?????? DTO ??????
   *
   * @throws Exception
   */
  @Test
  void testJpaDto() throws Exception {
    // given

    // when
    List<MemberDto> result =
        em.createQuery(
                "select new study.querydsl.dto.MemberDto(m.username, m.age) from Member m",
                MemberDto.class)
            .getResultList();

    for (MemberDto memberDto : result) {
      System.out.println("memberDto = " + memberDto);
    }

    // then
  }

  /**
   * DTO Projection by setter
   *
   * <pre>
   *     - ????????? ?????? ???????????? ????????????. (QueryDSL ??? ?????? ????????? ?????? ??? setter ??? ?????????.)
   * </pre>
   *
   * @throws Exception
   */
  @Test
  void testDtoProjectionBySetter() throws Exception {
    // given

    // when
    List<MemberDto> result =
        queryFactory
            .select(Projections.bean(MemberDto.class, member.username, member.age))
            .from(member)
            .fetch();

    for (MemberDto memberDto : result) {
      System.out.println("memberDto = " + memberDto);
    }

    // then
  }

  /**
   * DTO Projection by field
   *
   * <pre>
   *      - ????????? ?????? ???????????? ?????? ??????.
   *      - QueryDSL ??? ?????? Field ??? ????????????.
   *  </pre>
   *
   * @throws Exception
   */
  @Test
  void testDtoProjectionByField() throws Exception {
    // given

    // when
    List<MemberDto> result =
        queryFactory
            .select(Projections.fields(MemberDto.class, member.username, member.age))
            .from(member)
            .fetch();

    for (MemberDto memberDto : result) {
      System.out.println("memberDto = " + memberDto);
    }

    // field ??? ????????? ?????? ?????? as() ??? ???????????? ????????? ??? ??????.
    // sub query ?????? ????????? ?????? ????????? ?????? ??????.
    QMember subMember = new QMember("sub_member");

    List<UserDto> userResult =
        queryFactory
            .select(
                Projections.fields(
                    UserDto.class,
                    member.username.as("name"),
                    // ExpressionUtils.as(member.username, "name"), // ???????????? ??? ??? ?????????, ???????????????.
                    ExpressionUtils.as(
                        JPAExpressions.select(subMember.age.max()).from(subMember), "age")))
            .from(member)
            .fetch();

    for (UserDto userDto : userResult) {
      System.out.println("userDto = " + userDto);
    }

    // then
  }

  /**
   * DTO Projection by constructor
   *
   * <pre>
   *     - DTO ??? ????????? ?????? ????????????.
   *     - ???, ???????????? ??????????????? data type ??? ????????? ??????.
   * </pre>
   *
   * @throws Exception
   */
  @Test
  void testDtoProjectionByConstructor() throws Exception {
    // given

    // when
    List<MemberDto> result =
        queryFactory
            .select(Projections.constructor(MemberDto.class, member.username, member.age))
            .from(member)
            .fetch();

    for (MemberDto memberDto : result) {
      System.out.println("memberDto = " + memberDto);
    }

    List<UserDto> result1 =
        queryFactory
            .select(Projections.constructor(UserDto.class, member.username, member.age))
            .from(member)
            .fetch();

    for (UserDto userDto : result1) {
      System.out.println("userDto = " + userDto);
    }

    // then
  }

  /**
   * Projection by @QueryProjection
   *
   * <p>! Constructor Projection ?????? ?????????
   *
   * <pre>
   *     - ????????? ???????????? ????????? ?????? ??? ??????.
   *     - ??????
   *        - @QueryProjection ??? DTO ??? ??????????????????.
   *        - DTO ??? ????????? QueryDSL ??? ?????? ???????????? ????????? ??????. - DTO ??? ?????? ?????? layer ??? ????????? ??????????????? ?????? ???????????? ??? ??????.
   *        - ????????? ????????? ???????????????.
   * </pre>
   *
   * @throws Exception
   */
  @Test
  void testQueryProjection() throws Exception {
    // given

    // when
    List<MemberDto> result =
        queryFactory.select(new QMemberDto(member.username, member.age)).from(member).fetch();

    for (MemberDto memberDto : result) {
      System.out.println("memberDto = " + memberDto);
    }

    // then
  }

  /**
   * Dynamic Query - BooleanBuilder
   *
   * <pre>
   *     -
   * </pre>
   */
  @Test
  void testBooleanBuilder() throws Exception {
    // given

    String usernameParam = "member1";
    Integer ageParam = 10;

    // when

    List<Member> result = searchMember(usernameParam, ageParam);

    // then

    assertThat(result.size()).isEqualTo(1);
  }

  private List<Member> searchMember(String usernameParam, Integer ageParam) {

    BooleanBuilder builder = new BooleanBuilder();

    if (usernameParam != null) {
      builder.and(member.username.eq(usernameParam));
    }

    if (ageParam != null) {
      builder.and(member.age.eq(ageParam));
    }

    return queryFactory.selectFrom(member).where(builder).fetch();
  }

  /**
   * Dynamic Query - Where ?????? ???????????? ??????
   *
   * <pre>
   *     - where ?????? null ??? ????????? ?????? ????????????.
   *     - ?????? parameter ??? ????????? method ??? ????????? ????????????. - ???????????? ????????????.
   *     - ???????????? ????????????.
   * </pre>
   */
  @Test
  void testWhereParam() throws Exception {
    // given
    String usernameParam = "member1";
    Integer ageParam = 10;

    // when

    List<Member> result = searchMember2(usernameParam, ageParam);

    // then
    assertThat(result.size()).isEqualTo(1);
  }

  private List<Member> searchMember2(String usernameParam, Integer ageParam) {

    return queryFactory
        .selectFrom(member)
        .where(usernameEq(usernameParam), ageEq(ageParam))
        .fetch();
  }

  private BooleanExpression usernameEq(String usernameParam) {
    return usernameParam != null ? member.username.eq(usernameParam) : null;
  }

  private BooleanExpression ageEq(Integer ageParam) {
    return ageParam != null ? member.age.eq(ageParam) : null;
  }

  private Predicate allEq(String usernameParam, Integer ageParam) {
    return usernameEq(usernameParam).and(ageEq(ageParam)); // null ??? ?????????????????? ??????.
  }

  /**
   * ??????, ?????? ?????? ??????
   *
   * <pre>
   *     - JPA ??? ???????????? Persistence Context ???????????? DB query ??? ??????
   *     - ?????????, Bulk ?????? ??? Persistence Context ??? ?????? ????????? ?????? ??????????????????.
   * </pre>
   */
  @Test
  void testBulkUpdate() throws Exception {
    // given

    // when
    long count =
        queryFactory.update(member).set(member.username, "?????????").where(member.age.lt(28)).execute();

    em.flush();
    em.clear();

    List<Member> result = queryFactory.selectFrom(member).fetch();

    for (Member member1 : result) {
      System.out.println("member1 = " + member1);
    }

    // then
    assertThat(count).isEqualTo(2);
  }

  @Test
  void testBulkAdd() throws Exception {
    // given

    // when
    long count = queryFactory.update(member).set(member.age, member.age.add(1)).execute();

    // then
  }

  @Test
  void testBulkDelete() throws Exception {
    // given

    // when
    long count = queryFactory.delete(member).where(member.age.gt(18)).execute();

    // then
  }

  @Test
  void testSQLFunction() throws Exception {
    // given

    // when
    List<String> result =
        queryFactory
            .select(
                Expressions.stringTemplate(
                    "function('replace', {0}, {1}, {2})", member.username, "member", "M"))
            .from(member)
            .fetch();

    for (String s : result) {
      System.out.println("s = " + s);
    }


    // * ansi ?????? SQL function ??? ???????????? ??????.
    List<String> result1 =
        queryFactory
            .select(member.username)
            .from(member)
            // .where(
            //    member.username.eq(
            //        Expressions.stringTemplate("function('lower', {0})", member.username)))
            .where(member.username.eq(member.username.lower()))
            .fetch();

    for (String s : result1) {
      System.out.println("s = " + s);
    }

    // then
  }
}
