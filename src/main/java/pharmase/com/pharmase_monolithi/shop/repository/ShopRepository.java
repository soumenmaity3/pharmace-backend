package pharmase.com.pharmase_monolithi.shop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pharmase.com.pharmase_monolithi.auth.model.owner.ShopOwner;
import pharmase.com.pharmase_monolithi.shop.model.Shop;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShopRepository extends JpaRepository<Shop, UUID> {
    boolean existsByShopCode(String shopCode);

    boolean existsByOwnerAndShopNameIgnoreCaseAndAddressIgnoreCase(ShopOwner shopOwner, String shopName, String address);

    List<Shop> findAllByOwnerId(UUID id);

    Optional<Shop> findByIdAndOwner_Id(UUID shopId, UUID ownerId);
}
