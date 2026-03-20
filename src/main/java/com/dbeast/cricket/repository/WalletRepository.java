package com.dbeast.cricket.repository;

import com.dbeast.cricket.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletRepository extends JpaRepository<Wallet, Long> {
}
