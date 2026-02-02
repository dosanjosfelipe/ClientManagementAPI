package dev.felipe.clientmanagement.repository;

import dev.felipe.clientmanagement.model.Client;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {
    Optional<Client> findByEmail(@NotBlank @Email String email);

    Optional<Client> findByPhone(@Pattern(regexp = "^[0-9]+$")
                                 @NotBlank
                                 @Size(min = 10, max = 11) String phone);


    List<Client> getClientByOwner_Id(Long ownerId, Pageable pageable);

    @Query("SELECT COUNT(c) FROM Client c WHERE c.owner.id = :ownerId")
    int getClientSizeByOwner_Id(Long ownerId);
}
