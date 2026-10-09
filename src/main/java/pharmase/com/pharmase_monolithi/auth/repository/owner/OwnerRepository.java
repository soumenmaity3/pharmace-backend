package pharmase.com.pharmase_monolithi.auth.repository.owner;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pharmase.com.pharmase_monolithi.auth.model.owner.ShopOwner;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OwnerRepository extends JpaRepository<ShopOwner, UUID> {
    Optional<ShopOwner> findByEmailIgnoreCase(String normalizedEmail);
}
