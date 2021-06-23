package dev.highright96.springbatch.part4;

import java.time.LocalDate;
import java.util.Collection;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    Collection<User> findAllByUpdateDate(LocalDate now);
}
