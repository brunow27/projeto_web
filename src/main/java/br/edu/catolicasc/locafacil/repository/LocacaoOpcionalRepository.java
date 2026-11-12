package br.edu.catolicasc.locafacil.repository;

import br.edu.catolicasc.locafacil.entity.LocacaoOpcional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocacaoOpcionalRepository extends JpaRepository<LocacaoOpcional, Long> {

    List<LocacaoOpcional> findByLocacaoId(Long locacaoId);

    /** RF08: impede remover opcional já usado em locações. */
    boolean existsByOpcionalId(Long opcionalId);
}
