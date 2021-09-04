package study.querydsl.dto;

public class MemberSearchCondition {

  private String username;
  private String teamName;
  private Integer ageGoe;
  private Integer ageLoe;

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getTeamName() {
    return teamName;
  }

  public void setTeamName(String teamName) {
    this.teamName = teamName;
  }

  public Integer getAgeGoe() {
    return ageGoe;
  }

  public void setAgeGoe(Integer ageGoe) {
    this.ageGoe = ageGoe;
  }

  public Integer getAgeLoe() {
    return ageLoe;
  }

  public void setAgeLoe(Integer ageLoe) {
    this.ageLoe = ageLoe;
  }
}
