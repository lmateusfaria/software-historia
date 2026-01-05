package com.gestaofinanceirapessoal.services;

import com.gestaofinanceirapessoal.domains.Transacao;
import com.gestaofinanceirapessoal.domains.Usuario;
import com.gestaofinanceirapessoal.domains.dtos.TransacaoDTO;
import com.gestaofinanceirapessoal.domains.enums.TipoTransacao;
import com.gestaofinanceirapessoal.repositories.TransacaoRepository;
import com.gestaofinanceirapessoal.repositories.UsuarioRepository;
import com.gestaofinanceirapessoal.services.exceptions.ObjectNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransacaoService {

    @Autowired
    private TransacaoRepository transacaoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    public TransacaoDTO registrarTransacao(TransacaoDTO dto, String emailUsuario) {
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ObjectNotFoundException("Usuário não encontrado! Email: " + emailUsuario));

        Transacao t = new Transacao();
        t.setTipo(dto.getTipo());
        t.setValor(dto.getValor());
        t.setData(dto.getData() != null ? dto.getData() : LocalDateTime.now());
        t.setDescricao(dto.getDescricao());
        t.setUsuario(usuario);

        transacaoRepository.save(t);
        return new TransacaoDTO(t);
    }

    public List<TransacaoDTO> listarTransacoes(String emailUsuario, TipoTransacao tipo) {
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ObjectNotFoundException("Usuário não encontrado! Email: " + emailUsuario));

        List<Transacao> list = (tipo == null)
                ? transacaoRepository.findByUsuarioId(usuario.getId())
                : transacaoRepository.findByUsuarioIdAndTipo(usuario.getId(), tipo);

        return list.stream().map(TransacaoDTO::new).collect(Collectors.toList());
    }

    public BigDecimal calcularSaldo(String emailUsuario) {
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ObjectNotFoundException("Usuário não encontrado! Email: " + emailUsuario));
        return transacaoRepository.calcularSaldo(usuario.getId());
    }

    public List<TransacaoDTO> ultimasTransacoes(String emailUsuario, int limit) {
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ObjectNotFoundException("Usuário não encontrado! Email: " + emailUsuario));
        var page = PageRequest.of(0, Math.max(1, limit));
        return transacaoRepository.findByUsuarioIdOrderByDataDesc(usuario.getId(), page)
                .stream().map(TransacaoDTO::new).collect(Collectors.toList());
    }
}
