package com.example.ethereumwalletsystem.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.Where;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.UUID;

@Entity
@Where(clause = "deleted_at IS NULL")
@Getter
@Setter
@ToString
@NoArgsConstructor
public class Currency {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private UUID uuid = UUID.randomUUID();

    @NotNull
    private String symbol;

    private String contract;

    private int decimal = 18;

    private long lastBlockNumber = 0;

    @CreationTimestamp
    private Date createdAt;

    @UpdateTimestamp
    private Date updatedAt;

    public Currency(String symbol, String contract, int decimal) {
        this.symbol = symbol;
        this.contract = contract;
        this.decimal = decimal;
    }
}
