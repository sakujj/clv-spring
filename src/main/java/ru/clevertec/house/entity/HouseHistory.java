package ru.clevertec.house.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.clevertec.house.enumeration.PersonType;

import java.time.LocalDateTime;

@Entity
@Table(name = "house_history")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class HouseHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "house_id", nullable = false, updatable = false)
    private House house;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "person_id", nullable = false, updatable = false)
    private Person person;

    @Column(name = "date", nullable = false, updatable = false)
    private LocalDateTime date;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, updatable = false)
    private PersonType type;
}
