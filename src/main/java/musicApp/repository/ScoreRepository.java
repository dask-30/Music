package musicApp.repository;

import musicApp.model.structural.Score;
import musicApp.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScoreRepository extends JpaRepository<Score, Long> {
     List<Score> findAllByUserId(long id);
}
