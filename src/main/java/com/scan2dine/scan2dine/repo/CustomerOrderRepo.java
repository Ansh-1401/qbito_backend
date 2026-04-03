package com.scan2dine.scan2dine.repo;

import com.scan2dine.scan2dine.entity.CustomerOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CustomerOrderRepo extends JpaRepository<CustomerOrder, Long> {
    List<CustomerOrder> findByRestaurantId(Long restaurantId);
    List<CustomerOrder> findByRestaurantIdOrderByCreatedAtDesc(Long restaurantId);
    List<CustomerOrder> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<CustomerOrder> findByPaymentDoneTrue();
}
