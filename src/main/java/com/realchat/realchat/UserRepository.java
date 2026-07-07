package com.realchat.realchat;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/*
 * UserRepository
 *
 * User 엔티티의 DB 작업을 담당하는 인터페이스.
 * JpaRepository 상속으로 save(), findAll() 등은 자동 생성.
 *
 * findByUsername()은 직접 추가한 메서드.
 * 메서드 이름을 보고 Spring Data JPA가 자동으로 SQL을 만들어준다.
 *   findByUsername("코파")
 *   → SELECT * FROM users WHERE username = '코파'
 */

public interface UserRepository extends JpaRepository<User, Long> {
	
	Optional<User> findByUsername(String username);
}
