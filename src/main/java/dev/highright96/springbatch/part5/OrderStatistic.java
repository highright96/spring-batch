package dev.highright96.springbatch.part5;

import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
public class OrderStatistic {

    private String amount;

    private LocalDate date;

    @Builder
    public OrderStatistic(String amount, LocalDate date) {
        this.amount = amount;
        this.date = date;
    }
}
