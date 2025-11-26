package com.example.financery.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "TagTable")
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 15)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", nullable = false)
    @JsonBackReference
    private User user;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "TransactionTag",
            joinColumns = @JoinColumn(name = "tagId"),
            inverseJoinColumns = @JoinColumn(name = "transactionId")
    )
    private List<Transaction> transactions = new ArrayList<>();

    @Override
    public String toString() {
        return "Tag{"
                + "id=" + id
                + ", title='" + title + '\''
                + ", userId=" + (user != null ? user.getId() : null)
                + '}';
    }
}
