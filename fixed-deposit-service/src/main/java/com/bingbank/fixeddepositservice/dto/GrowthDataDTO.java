package com.bingbank.fixeddepositservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GrowthDataDTO {
    private Long fdId;
    private BigDecimal principalAmount;
    private BigDecimal currentValue;
    private BigDecimal maturityAmount;
    private LocalDate startDate;
    private LocalDate maturityDate;
    private Integer tenureYears;
    private BigDecimal interestRate;
    private List<DataPoint> growthPoints;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataPoint {
        private LocalDate date;
        private BigDecimal value;
        private Integer daysFromStart;
    }
}