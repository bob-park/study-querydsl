package study.querydsl.dto;

import com.querydsl.core.annotations.QueryProjection;

public class MemberDto {

  private String username;
  private int age;

  public MemberDto() {
  }

  @QueryProjection
  public MemberDto(String username, int age) {
    this.username = username;
    this.age = age;
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

  @Override
  public String toString() {
    return "MemberDto{" + "username='" + username + '\'' + ", age=" + age + '}';
  }
}
