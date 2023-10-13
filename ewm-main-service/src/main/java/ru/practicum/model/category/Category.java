package ru.practicum.model.category;

import lombok.*;

import javax.persistence.*;

@Table(name = "categories", schema = "public")
@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Category {
    @Id
    @Column(name = "category_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "category_name", unique = true)
    private String name;
}
