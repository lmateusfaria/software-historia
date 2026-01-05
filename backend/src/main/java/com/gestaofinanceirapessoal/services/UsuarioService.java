package com.gestaofinanceirapessoal.services;

import com.gestaofinanceirapessoal.domains.Usuario;
import com.gestaofinanceirapessoal.domains.dtos.UsuarioDTO;
import com.gestaofinanceirapessoal.repositories.UsuarioRepository;
import com.gestaofinanceirapessoal.services.exceptions.DataIntegrityViolationException;
import com.gestaofinanceirapessoal.services.exceptions.ObjectNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepo;

    @Autowired
    private PasswordEncoder encoder;

    public List<UsuarioDTO> findAll() {
        return usuarioRepo.findAll().stream()
                .map(UsuarioDTO::new)
                .collect(Collectors.toList());
    }

    public Usuario findById(Long id) {
        return usuarioRepo.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Usuário não encontrado! Id: " + id));
    }

    public Usuario findByCpf(String cpf) {
        return usuarioRepo.findByCpf(cpf)
                .orElseThrow(() -> new ObjectNotFoundException("Usuário não encontrado! CPF: " + cpf));
    }

    public Usuario findByEmail(String email) {
        return usuarioRepo.findByEmail(email)
                .orElseThrow(() -> new ObjectNotFoundException("Usuário não encontrado! Email: " + email));
    }

    public Usuario create(UsuarioDTO dto) {
        dto.setId(null);
        dto.setSenha(encoder.encode(dto.getSenha())); // ok ✅
        validaCpfEmail(dto);
        Usuario obj = new Usuario(dto);

        return usuarioRepo.save(obj);
    }

    public Usuario update(Long usuarioId, UsuarioDTO dto) {
        Usuario oldObj = findById(usuarioId);
        validaCpfEmail(dto);

        oldObj.setNome(dto.getNome());
        oldObj.setCpf(dto.getCpf());
        oldObj.setEmail(dto.getEmail());

        if (dto.getSenha() != null && !dto.getSenha().isBlank()) {
            oldObj.setSenha(encoder.encode(dto.getSenha()));
        }

        return usuarioRepo.save(oldObj);
    }

    public void delete(Long id) {
        Usuario obj = findById(id);

        if (!obj.getTransacoes().isEmpty()) {
            throw new DataIntegrityViolationException("Não é possível deletar o usuário que possui transações associadas.");
        }

        // Remover a entidade gerenciada para permitir cascade remove nas transações
        usuarioRepo.delete(obj);
    }

    private void validaCpfEmail(UsuarioDTO dto) {
        Optional<Usuario> obj = usuarioRepo.findByCpf(dto.getCpf());
        if (obj.isPresent() && !obj.get().getId().equals(dto.getId())) {
            throw new DataIntegrityViolationException("CPF já cadastrado no sistema");
        }

        obj = usuarioRepo.findByEmail(dto.getEmail());
        if (obj.isPresent() && !obj.get().getId().equals(dto.getId())) {
            throw new DataIntegrityViolationException("Email já cadastrado no sistema");
        }
    }
}
