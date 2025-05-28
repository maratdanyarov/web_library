package com.mdanyarov.weblibrary.dto;

import com.mdanyarov.weblibrary.entity.Order;

/**
 * DTO for order requests.
 * Represents the data structure for order request form.
 */
public class OrderRequest {
    private Order.OrderType orderType = Order.OrderType.HOME;
    private String notes;

    public Order.OrderType getOrderType() {
        return orderType;
    }

    public void setOrderType(Order.OrderType orderType) {
        this.orderType = orderType;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
