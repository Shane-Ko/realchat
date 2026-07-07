package com.realchat.realchat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/*
 * User 엔티티
 *
 * 회원 한 명의 정보를 담는 클래스.
 * MySQL의 users 테이블과 매핑된다.
 *
 * - @Table(name = "users"): 테이블 이름을 users로 지정
 *   (user는 MySQL 예약어라서 그대로 쓰면 에러남)
 * - username은 중복 불가 (unique = true)
 * - password는 암호화되어 저장됨
 */

@Entity
@Table(name="users")
public class User {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(unique = true, nullable = false)
	private String username;
	
	@Column(nullable = false)
	private String password;
	
	// 기본생성자
	public User() {
	}
	
	// 매개변수 생성자
	public User(String username, String password) {
		this.username = username;
		this.password = password;
	}
	
	//Getter
	public Long getId() {return id;}
	public String getUsername() {return username;}
	public String getPassword() {return password;}
}
