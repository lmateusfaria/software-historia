
package com.gestaofinanceirapessoal.resources;

import com.gestaofinanceirapessoal.domains.Usuario;
import com.gestaofinanceirapessoal.domains.dtos.UsuarioDTO;
import com.gestaofinanceirapessoal.services.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.gestaofinanceirapessoal.security.UserSS;
// Endpoint para retornar dados do usuário autenticado via JWT


@RestController
@RequestMapping(value = "/usuarios")
public class UsuarioResource {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    public ResponseEntity<List<UsuarioDTO>> findAll() {
        return ResponseEntity.ok().body(usuarioService.findAll());
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<UsuarioDTO> findById(@PathVariable Long id) {
        Usuario obj = usuarioService.findById(id);
        return ResponseEntity.ok().body(new UsuarioDTO(obj));
    }

    @GetMapping(value = "/cpf/{cpf}")
    public ResponseEntity<UsuarioDTO> findByCpf(@PathVariable String cpf) {
        Usuario obj = usuarioService.findByCpf(cpf);
        return ResponseEntity.ok().body(new UsuarioDTO(obj));
    }

    @GetMapping(value = "/email/{email}")
    public ResponseEntity<UsuarioDTO> findByEmail(@PathVariable String email) {
        Usuario obj = usuarioService.findByEmail(email);
        return ResponseEntity.ok().body(new UsuarioDTO(obj));
    }

    @PostMapping
    public ResponseEntity<Void> insert(@Valid @RequestBody UsuarioDTO objDto) {
        Usuario usuario = usuarioService.create(objDto);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(usuario.getId()).toUri();
        return ResponseEntity.created(uri).build();
    }

    @PutMapping(value = "/{id}")
    public ResponseEntity<UsuarioDTO> update(@PathVariable Long id, @Valid @RequestBody UsuarioDTO objDto) {
        Usuario obj = usuarioService.update(id, objDto);
        return ResponseEntity.ok().body(new UsuarioDTO(obj));
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {

        usuarioService.delete(id);
        return ResponseEntity.noContent().build();
    }

      @DeleteMapping(value = "/delete-account")
      public ResponseEntity<Void> deleteCurrent(@AuthenticationPrincipal UserSS user) {
          // Busca o usuário autenticado pelo e-mail presente no JWT e deleta sua conta
          Usuario obj = usuarioService.findByEmail(user.getUsername());
          usuarioService.delete(obj.getId());
          return ResponseEntity.noContent().build();
      }

    @GetMapping("/me")
    public ResponseEntity<UsuarioDTO> getMe(@AuthenticationPrincipal UserSS user) {
        Usuario obj = usuarioService.findByEmail(user.getUsername());
        return ResponseEntity.ok().body(new UsuarioDTO(obj));
    }

}
