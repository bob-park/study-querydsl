package study.querydsl.controller;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Member;
import study.querydsl.entity.Team;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;

@Profile("local")
@Component
public class InitMember {

  private final InitMemberService initMemberService;

  public InitMember(InitMemberService initMemberService) {
    this.initMemberService = initMemberService;
  }

  @PostConstruct
  public void init() {
    initMemberService.init();
  }

  @Component
  static class InitMemberService {
    private final EntityManager em;

    public InitMemberService(EntityManager em) {
      this.em = em;
    }

    @Transactional
    public void init() {
      Team teamA = new Team("teamA");
      Team teamB = new Team("teamB");

      em.persist(teamA);
      em.persist(teamB);

      for (int i = 0; i < 100; i++) {
        Team selectedTeam = i % 2 == 0 ? teamA : teamB;

        em.persist(new Member("member" + i, i, selectedTeam));
      }
    }
  }
}
