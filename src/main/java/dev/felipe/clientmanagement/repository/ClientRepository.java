package dev.felipe.clientmanagement.repository;

import dev.felipe.clientmanagement.model.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.stream.Stream;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {

    @Query("""
    SELECT c
    FROM Client c
    WHERE c.owner.id = :ownerId
      AND (
            :search IS NULL
         OR LOWER(c.name)  LIKE LOWER(CONCAT('%', :search, '%'))
         OR LOWER(c.email) LIKE LOWER(CONCAT('%', :search, '%'))
         OR c.phone        LIKE CONCAT('%', :search, '%')
      )
""")
    Page<Client> findSearchClientByOwner_Id(
            @Param("ownerId") Long ownerId,
            @Param("search") String search,
            Pageable pageable
    );

    @Query("SELECT c FROM Client c WHERE c.owner.id = :ownerId")
    Stream<Client> streamAllByOwnerId(
            @Param("ownerId") Long ownerId
    );

    boolean existsClientByEmail(String email);

    boolean existsClientByPhone(String phone);
}
