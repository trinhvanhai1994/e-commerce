package com.dragun.ecommerce.repository;

import com.dragun.ecommerce.integration.pancake.PancakeIntegrationConstants;
import com.dragun.ecommerce.model.entity.Order;
import com.dragun.ecommerce.model.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderId(String orderId);
    List<Order> findByCustomerPhone(String customerPhone);
    List<Order> findByStatus(OrderStatus status);
    List<Order> findAllByOrderByCreatedAtDesc();
    
    @Query("SELECT COUNT(o) FROM Order o WHERE DATE(o.createdAt) = :date")
    long countByCreatedAtDate(@Param("date") LocalDate date);
    
    Optional<Order> findByPancakeOrderId(String pancakeOrderId);
    List<Order> findByPancakeOrderIdIsNull();
    
    @Query("SELECT o FROM Order o WHERE o.pancakeSyncedAt IS NULL OR o.updatedAt > :updatedAt")
    List<Order> findOrdersNeedingSync(@Param("updatedAt") LocalDateTime updatedAt);

    @Query("SELECT DISTINCT o FROM Order o JOIN FETCH o.items i JOIN FETCH i.product WHERE o.orderId = :orderId")
    Optional<Order> findByOrderIdWithItems(@Param("orderId") String orderId);

    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.items i LEFT JOIN FETCH i.product WHERE o.pancakeOrderId = :pancakeOrderId")
    Optional<Order> findByPancakeOrderIdWithItems(@Param("pancakeOrderId") String pancakeOrderId);

    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.items i LEFT JOIN FETCH i.product WHERE o.id = :id")
    Optional<Order> findByIdWithItems(@Param("id") Long id);

    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.items i LEFT JOIN FETCH i.product WHERE o.orderType = '"
            + PancakeIntegrationConstants.ORDER_TYPE_PANCAKE
            + "' ORDER BY o.createdAt DESC")
    List<Order> findPancakeOrdersWithItems();
}
