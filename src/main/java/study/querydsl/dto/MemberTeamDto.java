package study.querydsl.dto;

import com.querydsl.core.annotations.QueryProjection;

public class MemberTeamDto {

  private Long memberId;
  private String username;
  private int age;
  private Long teamId;
  private String teamName;

  @QueryProjection
  public MemberTeamDto(Long memberId, String username, int age, Long teamId, String teamName) {
    this.memberId = memberId;
    this.username = username;
    this.age = age;
    this.teamId = teamId;
    this.teamName = teamName;
  }

  public Long getMemberId() {
    return memberId;
  }

  public void setMemberId(Long memberId) {
    this.memberId = memberId;
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

  public Long getTeamId() {
    return teamId;
  }

  public void setTeamId(Long teamId) {
    this.teamId = teamId;
  }

  public String getTeamName() {
    return teamName;
  }

  public void setTeamName(String teamName) {
    this.teamName = teamName;
  }
}
