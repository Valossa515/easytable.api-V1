package br.com.aftersunrise.easytable.borders.entities;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;

import java.util.UUID;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class DatabaseEntityBase {
    @Id
    @Builder.Default
    private String id = UUID.randomUUID().toString();
}