package net.cserny.backsantaspring.matches;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchRepository extends JpaRepository<MatchEntity, String> {
}
