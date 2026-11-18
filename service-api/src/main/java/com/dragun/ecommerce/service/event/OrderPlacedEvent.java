package com.dragun.ecommerce.service.event;

/**
 * Published by OrderService when a storefront order is saved. Carries the database id (not the entity):
 * listeners run after commit on another thread and load the order themselves.
 */
public record OrderPlacedEvent(Long orderId) {
}
