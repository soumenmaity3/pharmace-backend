package pharmase.com.pharmase_monolithi.inventory.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pharmase.com.pharmase_monolithi.inventory.model.Inventory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InventoryRepository extends JpaRepository<Inventory, UUID> {
    Optional<Inventory> findByShop_IdAndBatchNoIgnoreCase(UUID shopId, String batchNo);

    @Query("SELECT i FROM Inventory i WHERE i.shop.id = :shopId AND (" +
            "LOWER(i.medName) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
            "LOWER(i.genericName) LIKE LOWER(CONCAT('%', :q, '%')) OR " +   // fixed: was genricName
            "LOWER(i.brand) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
            "LOWER(i.batchNo) LIKE LOWER(CONCAT('%', :q, '%'))) " +
            "ORDER BY CASE WHEN LOWER(i.medName) LIKE LOWER(CONCAT(:q, '%')) THEN 0 ELSE 1 END, i.medName")
    List<Inventory> searchByShopAndQuery(@Param("shopId") UUID shopId, @Param("q") String q);

    List<Inventory> findByShopIdOrderByExpiryDate(UUID uuid, Pageable pageable);
}
