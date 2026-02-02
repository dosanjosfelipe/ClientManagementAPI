package dev.felipe.clientmanagement.repository;

import dev.felipe.clientmanagement.model.Client;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {
    Optional<Client> findByEmail(@NotBlank @Email String email);

    Optional<Client> findByPhone(@Pattern(regexp = "^[0-9]+$")
                                 @NotBlank
                                 @Size(min = 10, max = 11) String phone);

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
}
