package br.edu.catolicasc.locafacil.repository;

import br.edu.catolicasc.locafacil.entity.Filial;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FilialRepository extends JpaRepository<Filial, Long> {

    List<Filial> findByCidadeIgnoreCase(String cidade);
}
