package study.querydsl.repository.custom.impl;

import com.querydsl.core.QueryResults;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.data.support.PageableExecutionUtils;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.dto.QMemberTeamDto;
import study.querydsl.entity.Member;
import study.querydsl.repository.custom.MemberRepositoryCustom;

import java.util.List;

import static org.springframework.util.StringUtils.hasText;
import static study.querydsl.entity.QMember.member;
import static study.querydsl.entity.QTeam.team;

/**
 * QuerydslRepositorySupport
 *
 * <pre>
 *     - entity manager 를 얻어올 수 있기 때문에 바로 쓸 수 있다.
 *     - querydsl 이라는 객체도 사용할 수 있다.
 *     - QueryFactory 를 제공해주지 않는다.
 *     - JPAQueryFactory 없이 바로 from() 부터 사용할 수 있다. - select 는 맨 나중에 들어가버린다. 그래서 가독성이 떨어진다.
 *     - Spring Data 에서 페이징을 getQuerydsl().applyPagination() 으로 가능한데, 별로 도움이 안될 거 같다.
 *     - Spring data sort 기능이 정상 동작 안한다.
 * </pre>
 */
public class MemberRepositoryImpl extends QuerydslRepositorySupport
    implements MemberRepositoryCustom {

  private final JPAQueryFactory queryFactory;
  //
  //  public MemberRepositoryImpl(EntityManager em) {
  //    this.queryFactory = new JPAQueryFactory(em);
  //  }

  public MemberRepositoryImpl() {
    super(Member.class);
    this.queryFactory = new JPAQueryFactory(getEntityManager());
  }

  @Override
  public List<MemberTeamDto> search(MemberSearchCondition condition) {
    //    return queryFactory
    //        .select(
    //            new QMemberTeamDto(
    //                member.id.as("memberId"),
    //                member.username,
    //                member.age,
    //                team.id.as("teamId"),
    //                team.name.as("teamName")))
    //        .from(member)
    //        .leftJoin(member.team, team)
    //        .where(
    //            usernameEq(condition.getUsername()),
    //            teamNameEq(condition.getTeamName()),
    //            ageGoe(condition.getAgeGoe()),
    //            ageLoe(condition.getAgeLoe()))
    //        .fetch();
    return from(member)
        .leftJoin(member.team, team)
        .where(
            usernameEq(condition.getUsername()),
            teamNameEq(condition.getTeamName()),
            ageGoe(condition.getAgeGoe()),
            ageLoe(condition.getAgeLoe()))
        .select(
            new QMemberTeamDto(
                member.id.as("memberId"),
                member.username,
                member.age,
                team.id.as("teamId"),
                team.name.as("teamName")))
        .fetch();
  }

  @Override
  public Page<MemberTeamDto> searchPageSimple(MemberSearchCondition condition, Pageable pageable) {
    //    QueryResults<MemberTeamDto> results =
    //        queryFactory
    //            .select(
    //                new QMemberTeamDto(
    //                    member.id.as("memberId"),
    //                    member.username,
    //                    member.age,
    //                    team.id.as("teamId"),
    //                    team.name.as("teamName")))
    //            .from(member)
    //            .leftJoin(member.team, team)
    //            .where(
    //                usernameEq(condition.getUsername()),
    //                teamNameEq(condition.getTeamName()),
    //                ageGoe(condition.getAgeGoe()),
    //                ageLoe(condition.getAgeLoe()))
    //            .offset(pageable.getOffset())
    //            .limit(pageable.getPageSize())
    //            .fetchResults();

    JPQLQuery<MemberTeamDto> jpqlQuery =
        from(member)
            .leftJoin(member.team, team)
            .where(
                usernameEq(condition.getUsername()),
                teamNameEq(condition.getTeamName()),
                ageGoe(condition.getAgeGoe()),
                ageLoe(condition.getAgeLoe()))
            .select(
                new QMemberTeamDto(
                    member.id.as("memberId"),
                    member.username,
                    member.age,
                    team.id.as("teamId"),
                    team.name.as("teamName")));

    QueryResults<MemberTeamDto> results =
        getQuerydsl().applyPagination(pageable, jpqlQuery).fetchResults(); // 좋지는 않다.

    List<MemberTeamDto> content = results.getResults();
    long total = results.getTotal();

    return new PageImpl<>(content, pageable, total);
  }

  @Override
  public Page<MemberTeamDto> searchPageComplex(MemberSearchCondition condition, Pageable pageable) {

    List<MemberTeamDto> content =
        queryFactory
            .select(
                new QMemberTeamDto(
                    member.id.as("memberId"),
                    member.username,
                    member.age,
                    team.id.as("teamId"),
                    team.name.as("teamName")))
            .from(member)
            .leftJoin(member.team, team)
            .where(
                usernameEq(condition.getUsername()),
                teamNameEq(condition.getTeamName()),
                ageGoe(condition.getAgeGoe()),
                ageLoe(condition.getAgeLoe()))
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

    JPAQuery<Long> countQuery =
        queryFactory
            .select(member.id.count())
            .from(member)
            .leftJoin(member.team, team)
            .where(
                usernameEq(condition.getUsername()),
                teamNameEq(condition.getTeamName()),
                ageGoe(condition.getAgeGoe()),
                ageLoe(condition.getAgeLoe()));

    // 첫 페이지 - 전체 content 개수가 limit 보다 작을 경우 count query 실행 안함
    // 마지막 페이지 - count query 실행 안함
    return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);

    //    return new PageImpl<>(content, pageable, total != null ? total : 0);
  }

  private BooleanExpression usernameEq(String username) {
    return hasText(username) ? member.username.eq(username) : null;
  }

  private BooleanExpression teamNameEq(String teamName) {
    return hasText(teamName) ? team.name.eq(teamName) : null;
  }

  private BooleanExpression ageGoe(Integer ageGoe) {
    return ageGoe != null ? member.age.goe(ageGoe) : null;
  }

  private BooleanExpression ageLoe(Integer ageLoe) {
    return ageLoe != null ? member.age.loe(ageLoe) : null;
  }
}
