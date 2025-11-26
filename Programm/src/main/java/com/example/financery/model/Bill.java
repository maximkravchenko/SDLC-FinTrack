package com.example.financery.model;

import com.example.financery.exception.InvalidInputException;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "BillTable")
public class Bill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false)
    private double balance = 0.0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", nullable = false)
    @JsonBackReference
    private User user;

    @OneToMany(mappedBy = "bill",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    private List<Transaction> transactions = new ArrayList<>();

    public void addAmount(double amount) {
        this.balance += amount;
        if (user != null) {
            user.setBalance(user.getBalance() + amount);
        }
    }

    public void subtractAmount(double amount) {
        if (this.balance >= amount) {
            this.balance -= amount;
            if (user != null) {
                user.setBalance(user.getBalance() - amount);
            }
        } else {
            throw new InvalidInputException("Недостаточный остаток на счету");
        }
    }

    @Override
    public String toString() {
        return "Bill{"
                + "id=" + id
                + ", name='" + name + '\''
                + ", user=" + (user != null ? "User{id=" + user.getId() + "}" : null)
                + '}';
    }
}
