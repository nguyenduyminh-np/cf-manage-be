package com.duyminhdev.cf_manager.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "invoice_sequence")
@Getter
@Setter
public class InvoiceSequence {

    @Id
    @Column(name = "seq_date")
    private LocalDate seqDate;

    @Column(name = "current_value", nullable = false)
    private Long currentValue;

}