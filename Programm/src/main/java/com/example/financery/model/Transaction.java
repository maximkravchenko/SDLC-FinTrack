package com.example.financery.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Entity
@Table(name = "TransactionTable")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    private String description;

    @Column(nullable = false)
    private boolean type;

    @Column(nullable = false)
    private double amount;

    @Column(nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy")
    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", nullable = false)
    @JsonBackReference
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "billId", nullable = false)
    @JsonBackReference
    private Bill bill;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "TransactionTag",
            joinColumns = @JoinColumn(name = "transactionId"),
            inverseJoinColumns = @JoinColumn(name = "tagId")
    )
    private List<Tag> tags = new ArrayList<>();

    @Override
    public String toString() {
        return "Transaction{"
                + "id=" + id
                + ", name='" + name + '\''
                + ", description='" + description + '\''
                + ", type=" + type
                + ", amount=" + amount
                + ", date=" + date
                + ", userId=" + (user != null ? user.getId() : null)
                + ", billId=" + (bill != null ? bill.getId() : null)
                + ", tags=" + (tags != null ? tags.stream()
                .map(tag -> "Tag{id=" + tag
                        .getId() + "}").collect(Collectors.toList()) : null)
                + '}';
    }
}
