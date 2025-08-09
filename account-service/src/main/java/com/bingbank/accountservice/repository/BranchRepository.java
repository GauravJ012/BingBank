package com.bingbank.accountservice.repository;

import com.bingbank.accountservice.model.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BranchRepository extends JpaRepository<Branch, Long> {
    
    Optional<Branch> findByBranchCode(String branchCode);
}