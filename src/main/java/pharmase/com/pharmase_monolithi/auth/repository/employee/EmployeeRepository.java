package pharmase.com.pharmase_monolithi.auth.repository.employee;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pharmase.com.pharmase_monolithi.auth.model.employees.ShopEmployee;
import pharmase.com.pharmase_monolithi.shop.model.Shop;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeeRepository extends JpaRepository<ShopEmployee, UUID> {
    Optional<ShopEmployee> findByEmailIgnoreCase(String normalizedEmail);

    Optional<ShopEmployee> findByEmailAndShopId(String email, UUID shopId);

    List<ShopEmployee> findByEmailIgnoreCaseAndShopId(
            String email,
            UUID shopId
    );

    List<ShopEmployee> findByShopId(UUID id);
}
