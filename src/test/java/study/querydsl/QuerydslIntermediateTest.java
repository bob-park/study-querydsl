package study.querydsl;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
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

  /**
   * 순수 JPA 에서 DTO 반환
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
   *     - 반드시 기본 생성사가 필요하다. (QueryDSL 이 기본 생성자 생성 후 setter 로 넣는다.)
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
   *      - 이것은 기본 생성자가 필요 없다.
   *      - QueryDSL 이 바로 Field 에 들어간다.
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

    // field 의 이름이 다를 경우 as() 를 이용해서 맞춰줄 수 있다.
    // sub query 하여 별칭을 줘서 넣어줄 수도 있다.
    QMember subMember = new QMember("sub_member");

    List<UserDto> userResult =
        queryFactory
            .select(
                Projections.fields(
                    UserDto.class,
                    member.username.as("name"),
                    // ExpressionUtils.as(member.username, "name"), // 이렇게도 할 수 있지만, 지저분하다.
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
   *     - DTO 의 생성자 대로 생성한다.
   *     - 단, 생성자의 파라미터에 data type 이 맞아야 한다.
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
   * <p>! Constructor Projection 과의 차이점
   *
   * <pre>
   *     - 컴파일 단계에서 오류를 잡을 수 있다.
   *     - 단점
   *        - @QueryProjection 를 DTO 에 추가해야한다.
   *        - DTO 에 대해서 QueryDSL 에 대한 의존성이 가지게 된다. - DTO 인 경우 여러 layer 에 거쳐서 사용하니까 너무 치명적일 수 있다.
   *        - 설계시 고민을 해봐야한다.
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
}
