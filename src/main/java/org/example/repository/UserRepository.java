package org.example.repository;

import org.example.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    // A Spring a metódus neve alapján kitalálja az SQL lekérdezést! (SELECT * FROM app_user WHERE username = ?)
    User findByUsername(String username);
}