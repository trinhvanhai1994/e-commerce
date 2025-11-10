package com.dragun.ecommerce.integration.pancake.service;

import com.dragun.ecommerce.integration.config.PancakeIntegrationConfig;
import com.dragun.ecommerce.integration.pancake.client.PancakeApiClient;
import com.dragun.ecommerce.integration.pancake.dto.PancakeOrderDto;
import com.dragun.ecommerce.integration.pancake.mapper.OrderMapper;
import com.dragun.ecommerce.integration.pancake.model.PancakeOrderMapping;
import com.dragun.ecommerce.integration.pancake.model.PancakeSyncLog;
import com.dragun.ecommerce.integration.pancake.repository.PancakeOrderMappingRepository;
import com.dragun.ecommerce.integration.pancake.repository.PancakeSyncLogRepository;
import com.dragun.ecommerce.model.entity.Order;
import com.dragun.ecommerce.model.entity.OrderItem;
import com.dragun.ecommerce.model.entity.Product;
import com.dragun.ecommerce.repository.OrderRepository;
import com.dragun.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PancakeOrderSyncService {
    
    private final PancakeApiClient pancakeApiClient;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final OrderMapper orderMapper;
    private final PancakeOrderMappingRepository mappingRepository;
    private final PancakeSyncLogRepository syncLogRepository;
    private final PancakeIntegrationConfig config;
    
    /**
     * Sync orders from Pancake to Thi Yen
     */
    @Transactional
    public int syncFromPancake() {
        if (!config.getSync().getEnabled() || 
            (!config.getSync().getDirection().equals("FROM_PANCAKE") && 
             !config.getSync().getDirection().equals("BIDIRECTIONAL"))) {
            log.info("Sync from Pancake is disabled");
            return 0;
        }
        
        try {
            List<PancakeOrderDto> pancakeOrders = pancakeApiClient.getOrders().block();
            if (pancakeOrders == null || pancakeOrders.isEmpty()) {
                log.info("No orders found in Pancake");
                return 0;
            }
            
            int syncedCount = 0;
            for (PancakeOrderDto pancakeOrder : pancakeOrders) {
                try {
                    syncOrderFromPancake(pancakeOrder);
                    syncedCount++;
                    logSync("ORDER", pancakeOrder.getId(), "FROM_PANCAKE", "SUCCESS", null);
                } catch (Exception e) {
                    log.error("Error syncing order {} from Pancake: {}", pancakeOrder.getId(), e.getMessage());
                    logSync("ORDER", pancakeOrder.getId(), "FROM_PANCAKE", "FAILED", e.getMessage());
                }
            }
            
            log.info("Synced {} orders from Pancake", syncedCount);
            return syncedCount;
        } catch (Exception e) {
            log.error("Error syncing orders from Pancake: {}", e.getMessage());
            return 0;
        }
    }
    
    /**
     * Sync orders from Thi Yen to Pancake
     */
    @Transactional
    public int syncToPancake() {
        if (!config.getSync().getEnabled() || 
            (!config.getSync().getDirection().equals("TO_PANCAKE") && 
             !config.getSync().getDirection().equals("BIDIRECTIONAL"))) {
            log.info("Sync to Pancake is disabled");
            return 0;
        }
        
        try {
            // Get orders that need syncing (not synced or updated after last sync)
            List<Order> ordersToSync = orderRepository.findOrdersNeedingSync(
                    LocalDateTime.now().minusDays(1));
            
            if (ordersToSync.isEmpty()) {
                log.info("No orders to sync to Pancake");
                return 0;
            }
            
            int syncedCount = 0;
            for (Order order : ordersToSync) {
                try {
                    syncOrderToPancake(order);
                    syncedCount++;
                    logSync("ORDER", order.getOrderId(), "TO_PANCAKE", "SUCCESS", null);
                } catch (Exception e) {
                    log.error("Error syncing order {} to Pancake: {}", order.getOrderId(), e.getMessage());
                    logSync("ORDER", order.getOrderId(), "TO_PANCAKE", "FAILED", e.getMessage());
                }
            }
            
            log.info("Synced {} orders to Pancake", syncedCount);
            return syncedCount;
        } catch (Exception e) {
            log.error("Error syncing orders to Pancake: {}", e.getMessage());
            return 0;
        }
    }
    
    /**
     * Sync a single order from Pancake
     */
    @Transactional
    public Order syncOrderFromPancake(PancakeOrderDto pancakeOrder) {
        // Check if order already exists by pancake_order_id
        Optional<Order> existingOrderOpt = orderRepository.findByPancakeOrderId(pancakeOrder.getId());
        
        Order order;
        if (existingOrderOpt.isPresent()) {
            // Update existing order
            order = existingOrderOpt.get();
            orderMapper.updateThiYenOrder(order, pancakeOrder);
        } else {
            // Check mapping table
            Optional<PancakeOrderMapping> mappingOpt = mappingRepository.findByPancakeOrderId(pancakeOrder.getId());
            if (mappingOpt.isPresent()) {
                order = mappingOpt.get().getLocalOrder();
                orderMapper.updateThiYenOrder(order, pancakeOrder);
            } else {
                // Create new order
                order = orderMapper.toThiYenOrder(pancakeOrder);
                
                // Create order items
                if (pancakeOrder.getItems() != null && !pancakeOrder.getItems().isEmpty()) {
                    List<OrderItem> orderItems = orderMapper.createOrderItemsFromPancake(
                            order, 
                            pancakeOrder.getItems(),
                            this::findProductByPancakeId
                    );
                    order.setItems(orderItems);
                }
            }
        }
        
        order = orderRepository.save(order);
        
        // Update or create mapping
        Optional<PancakeOrderMapping> mappingOpt = mappingRepository.findByLocalOrderId(order.getId());
        PancakeOrderMapping mapping;
        if (mappingOpt.isPresent()) {
            mapping = mappingOpt.get();
            mapping.setPancakeOrderId(pancakeOrder.getId());
        } else {
            mapping = PancakeOrderMapping.builder()
                    .localOrder(order)
                    .pancakeOrderId(pancakeOrder.getId())
                    .build();
        }
        mapping.setLastSyncedAt(LocalDateTime.now());
        mappingRepository.save(mapping);
        
        return order;
    }
    
    /**
     * Sync a single order to Pancake
     */
    @Transactional
    public PancakeOrderDto syncOrderToPancake(Order order) {
        PancakeOrderDto pancakeOrder = orderMapper.toPancakeOrder(order);
        
        if (order.getPancakeOrderId() != null) {
            // Update existing order in Pancake
            pancakeOrder = pancakeApiClient.updateOrder(order.getPancakeOrderId(), pancakeOrder).block();
        } else {
            // Create new order in Pancake
            pancakeOrder = pancakeApiClient.createOrder(pancakeOrder).block();
            if (pancakeOrder != null && pancakeOrder.getId() != null) {
                order.setPancakeOrderId(pancakeOrder.getId());
                order.setPancakeSyncedAt(LocalDateTime.now());
                orderRepository.save(order);
                
                // Create mapping
                PancakeOrderMapping mapping = PancakeOrderMapping.builder()
                        .localOrder(order)
                        .pancakeOrderId(pancakeOrder.getId())
                        .build();
                mappingRepository.save(mapping);
            }
        }
        
        if (pancakeOrder != null) {
            order.setPancakeSyncedAt(LocalDateTime.now());
            orderRepository.save(order);
        }
        
        return pancakeOrder;
    }
    
    private Product findProductByPancakeId(String pancakeProductId) {
        return productRepository.findByPancakeProductId(pancakeProductId).orElse(null);
    }
    
    private void logSync(String entityType, String entityId, String direction, String status, String errorMessage) {
        PancakeSyncLog log = PancakeSyncLog.builder()
                .entityType(entityType)
                .entityId(entityId)
                .syncDirection(direction)
                .status(status)
                .errorMessage(errorMessage)
                .build();
        syncLogRepository.save(log);
    }
}

