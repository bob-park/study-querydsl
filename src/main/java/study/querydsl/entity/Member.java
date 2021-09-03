package study.querydsl.entity;

import javax.persistence.*;

@Entity
public class Member {

  @Id
  @GeneratedValue
  @Column(name = "member_id")
  private Long id;

  private String username;

  private int age;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "team_ID")
  private Team team;

  protected Member() {}

  public Member(String username) {
    this(username, 0);
  }

  public Member(String username, int age) {
    this(username, age, null);
  }

  public Member(String username, int age, Team team) {
    this.username = username;
    this.age = age;
    if (team != null) {
      changeTeam(team);
    }
  }

  public void changeTeam(Team team) {
    this.team = team;

    team.getMembers().add(this);
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public int getAge() {
    return age;
  }

  public void setAge(int age) {
    this.age = age;
  }

  public Team getTeam() {
    return team;
  }

  @Override
  public String toString() {
    return "Member{" + "id=" + id + ", username='" + username + '\'' + ", age=" + age + '}';
  }
}
