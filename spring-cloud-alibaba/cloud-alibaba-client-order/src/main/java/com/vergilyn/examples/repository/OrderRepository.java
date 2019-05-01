package com.vergilyn.examples.repository;

import com.vergilyn.examples.entity.Order;

import org.springframework.data.repository.CrudRepository;

public interface OrderRepository extends CrudRepository<Order, Integer> {
}
