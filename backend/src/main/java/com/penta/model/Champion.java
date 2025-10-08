package com.penta.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "champions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Champion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private Integer championId;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private String title;
    
    @Column(nullable = false)
    private String role; // TOP, JUNGLE, MID, ADC, SUPPORT
    
    @Column(nullable = false)
    private String lane; // TOP, JUNGLE, MID, BOTTOM
    
    @Column
    private String imageUrl;
    
    @Column
    private String splashUrl;
    
    @Column
    private Double winRate;
    
    @Column
    private Double pickRate;
    
    @Column
    private Double banRate;
    
    @Column
    private Integer tier; // 1-5 (S, A, B, C, D)
    
    @Column
    private String tags; // Comma-separated tags like "Fighter,Assassin"
}
