package br.com.fiap.tds.tdspa.javaadv.nuclearcase.resources.dtos;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CasoInvestigativoRequestDTO(
        @NotBlank(message = "O título é obrigatório")
        @Size(max = 150, message = "O título deve ter até 150 caracteres")
        String titulo,

        @NotBlank(message = "A descrição é obrigatória")
        @Size(max = 1000, message = "A descrição deve ter até 1000 caracteres")
        String descricao,

        @NotBlank(message = "A categoria é obrigatória")
        @Size(max = 50, message = "A categoria deve ter até 50 caracteres")
        String categoria,

        @NotNull(message = "A dificuldade é obrigatória")
        @Min(value = 1, message = "A dificuldade mínima é 1")
        @Max(value = 5, message = "A dificuldade máxima é 5")
        Integer dificuldade,

        @NotBlank(message = "A explicação científica é obrigatória")
        @Size(max = 2000, message = "A explicação científica deve ter até 2000 caracteres")
        String explicacaoCientifica
) {
}
