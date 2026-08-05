package com.example.splitwise.service;

import com.example.splitwise.domain.Money;
import com.example.splitwise.domain.SplitInput;

import java.util.List;
import java.util.Map;

interface SplitStrategy {
    Map<String, Money> split(Money total, List<SplitInput> inputs);
}
