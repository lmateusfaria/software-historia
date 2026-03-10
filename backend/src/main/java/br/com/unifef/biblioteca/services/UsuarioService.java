import br.com.unifef.biblioteca.domains.enums.Perfil;
import br.com.unifef.biblioteca.security.UserSS;
import br.com.unifef.biblioteca.services.exceptions.AuthorizationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import br.com.unifef.biblioteca.services.exceptions.DataIntegrityViolationException;
import br.com.unifef.biblioteca.services.exceptions.ObjectNotFoundException;
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
        dto.setSenha(encoder.encode(dto.getSenha()));
        validaCpfEmail(dto);

        Perfil perfilDesejado = dto.getPerfil();
        boolean usuarioLogadoPodeCadastrar = false;

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            UserSS userSS = (UserSS) auth.getPrincipal();
            Usuario currentUser = findByEmail(userSS.getUsername());
            usuarioLogadoPodeCadastrar = currentUser.getPodeCadastrar();
        }

        // Regra: Público só pode cadastrar PESQUISADOR.
        // PROFESSOR/ALUNO exige que quem está cadastrando tenha a flag podeCadastrar = true.
        if (perfilDesejado != Perfil.PESQUISADOR && !usuarioLogadoPodeCadastrar) {
            throw new AuthorizationException("Acesso negado. Apenas usuários autorizados podem cadastrar Professores ou Alunos.");
        }

        // Garante que o campo podeCadastrar só seja true se quem cadastrou tiver permissão para isso
        if (!usuarioLogadoPodeCadastrar) {
            dto.setPodeCadastrar(false);
        }

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
