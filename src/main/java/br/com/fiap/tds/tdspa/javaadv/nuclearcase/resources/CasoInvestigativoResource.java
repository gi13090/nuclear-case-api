package br.com.fiap.tds.tdspa.javaadv.nuclearcase.resources;

import br.com.fiap.tds.tdspa.javaadv.nuclearcase.domainmodel.entities.CasoInvestigativo;
import br.com.fiap.tds.tdspa.javaadv.nuclearcase.resources.dtos.CasoInvestigativoRequestDTO;
import br.com.fiap.tds.tdspa.javaadv.nuclearcase.resources.dtos.CasoInvestigativoResponseDTO;
import br.com.fiap.tds.tdspa.javaadv.nuclearcase.services.CasoInvestigativoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.ProblemDetail;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/casos")
@RequiredArgsConstructor
@Tag(name = "Casos investigativos", description = "Gerenciamento de investigações educativas sobre tecnologia nuclear")
@ApiResponses({@ApiResponse(responseCode = "400", description = "Entrada inválida; resposta ProblemDetail", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "500", description = "Erro interno; resposta ProblemDetail sem detalhes técnicos", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))})
public class CasoInvestigativoResource {
    private final CasoInvestigativoService service;

    @GetMapping
    @Operation(summary = "Listar casos", description = "Retorna todos os casos; lista vazia quando não há registros.")
    @ApiResponse(responseCode = "200", description = "Lista de casos")
    public List<CasoInvestigativoResponseDTO> listar() {
        return service.listar().stream().map(CasoInvestigativoResponseDTO::fromEntity).toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar caso por UUID")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Caso encontrado"),
            @ApiResponse(responseCode = "404", description = "Caso inexistente", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))})
    public CasoInvestigativoResponseDTO buscarPorId(@Parameter(description = "UUID do caso") @PathVariable UUID id) {
        return CasoInvestigativoResponseDTO.fromEntity(service.buscarPorId(id));
    }

    @GetMapping("/buscar")
    @Operation(summary = "Buscar por trecho do título", description = "Busca sem distinguir maiúsculas e minúsculas; sem resultados retorna lista vazia.")
    @ApiResponse(responseCode = "200", description = "Casos encontrados ou lista vazia")
    public List<CasoInvestigativoResponseDTO> buscar(@Parameter(description = "Trecho obrigatório do título", example = "alimento")
            @RequestParam @NotBlank @Size(max = 150) String titulo) {
        return service.buscarPorTitulo(titulo).stream().map(CasoInvestigativoResponseDTO::fromEntity).toList();
    }

    @PostMapping
    @Operation(summary = "Cadastrar caso", description = "Gera UUID e retorna o endereço do novo caso no cabeçalho Location.")
    @ApiResponse(responseCode = "201", description = "Caso criado")
    public ResponseEntity<CasoInvestigativoResponseDTO> criar(@Valid @RequestBody CasoInvestigativoRequestDTO dto) {
        var salvo = service.criar(toEntity(dto));
        var location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(salvo.getId()).toUri();
        return ResponseEntity.created(location).body(CasoInvestigativoResponseDTO.fromEntity(salvo));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar caso", description = "Substitui todos os campos de conteúdo do caso existente, preservando seu UUID.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Caso atualizado"),
            @ApiResponse(responseCode = "404", description = "Caso inexistente", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))})
    public CasoInvestigativoResponseDTO atualizar(@PathVariable UUID id, @Valid @RequestBody CasoInvestigativoRequestDTO dto) {
        return CasoInvestigativoResponseDTO.fromEntity(service.atualizar(id, toEntity(dto)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir caso")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Caso excluído, sem corpo", content = @Content),
            @ApiResponse(responseCode = "404", description = "Caso inexistente", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))})
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }

    private CasoInvestigativo toEntity(CasoInvestigativoRequestDTO dto) {
        var caso = new CasoInvestigativo();
        caso.setTitulo(dto.titulo());
        caso.setDescricao(dto.descricao());
        caso.setCategoria(dto.categoria());
        caso.setDificuldade(dto.dificuldade());
        caso.setExplicacaoCientifica(dto.explicacaoCientifica());
        return caso;
    }
}
