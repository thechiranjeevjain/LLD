package com.chiranjeev.lld.risk;
import java.util.List;
public record RiskDecision(boolean accepted, List<Violation> violations) { public RiskDecision { violations=List.copyOf(violations); } }
