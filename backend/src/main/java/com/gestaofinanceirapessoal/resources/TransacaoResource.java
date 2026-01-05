package com.gestaofinanceirapessoal.resources;

import com.gestaofinanceirapessoal.domains.dtos.TransacaoDTO;
import com.gestaofinanceirapessoal.domains.enums.TipoTransacao;
import com.gestaofinanceirapessoal.security.UserSS;
import com.gestaofinanceirapessoal.services.TransacaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/transacoes")
public class TransacaoResource {

    @Autowired
    private TransacaoService transacaoService;

    @PostMapping
    public ResponseEntity<TransacaoDTO> criar(@RequestBody TransacaoDTO dto,
                                              @AuthenticationPrincipal UserSS user) {
        TransacaoDTO criada = transacaoService.registrarTransacao(dto, user.getUsername());
        return ResponseEntity.ok(criada);
    }

    @GetMapping("/minhas")
    public ResponseEntity<List<TransacaoDTO>> minhas(@AuthenticationPrincipal UserSS user,
                                                     @RequestParam(value = "tipo", required = false) TipoTransacao tipo) {
        return ResponseEntity.ok(transacaoService.listarTransacoes(user.getUsername(), tipo));
    }

    @GetMapping("/saldo")
    public ResponseEntity<BigDecimal> saldo(@AuthenticationPrincipal UserSS user) {
        return ResponseEntity.ok(transacaoService.calcularSaldo(user.getUsername()));
    }

    @GetMapping("/minhas/ultimas")
    public ResponseEntity<List<TransacaoDTO>> ultimas(@AuthenticationPrincipal UserSS user,
                                                      @RequestParam(name = "limit", defaultValue = "5") int limit) {
        return ResponseEntity.ok(transacaoService.ultimasTransacoes(user.getUsername(), limit));
    }
}
