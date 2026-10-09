package pharmase.com.pharmase_monolithi.billing.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pharmase.com.pharmase_monolithi.billing.model.Billing;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BillingRepository extends JpaRepository<Billing, Long> {
    @EntityGraph(attributePaths = {"medicineList", "medicineList.inventory", "cashier", "updatedBy", "shop"})
    List<Billing> findByShop_IdOrderByCreateDateDesc(UUID shopId);

    @EntityGraph(attributePaths = {"medicineList", "medicineList.inventory", "cashier", "updatedBy", "shop"})
    Optional<Billing> findById(Long id);
}
