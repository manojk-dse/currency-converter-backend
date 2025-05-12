package com.example.currencyconverter.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.currencyconverter.data.model.Currency;

@Repository
public interface CurrencyRepository extends JpaRepository<Currency, String> {
}
