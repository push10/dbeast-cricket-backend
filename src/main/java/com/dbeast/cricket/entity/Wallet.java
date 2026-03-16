package com.dbeast.cricket.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class Wallet {

    @Id
    @GeneratedValue
    private Long id;

    private Long playerId;

    private Double balance = 0.0;

}