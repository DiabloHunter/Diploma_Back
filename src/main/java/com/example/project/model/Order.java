package com.example.project.model;

import org.hibernate.annotations.GenericGenerator;
import org.joda.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    private String id;

    @Column(name = "price")
    private double price;

    @ManyToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @OneToMany
    @MapKey(name = "id")
    private List<OrderUnit> orderUnits;

    @Column(name = "order_state")
    private OrderState orderState;

    public Order(double price, User user, LocalDateTime createdDate, List<OrderUnit> orderUnits, OrderState orderState) {
        this.price = price;
        this.user = user;
        this.createdDate = createdDate;
        this.orderUnits = orderUnits;
        this.orderState = orderState;
    }

    public Order() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public List<OrderUnit> getOrderUnits() {
        return orderUnits;
    }

    public void setOrderUnits(List<OrderUnit> orderUnits) {
        this.orderUnits = orderUnits;
    }

    public OrderState getOrderState() {
        return orderState;
    }

    public void setOrderState(OrderState orderState) {
        this.orderState = orderState;
    }
}
