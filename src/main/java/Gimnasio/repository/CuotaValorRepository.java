package Gimnasio.repository;

import Gimnasio.model.CuotaValor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CuotaValorRepository extends JpaRepository<CuotaValor, Long> {
    Optional<CuotaValor> findTopByOrderByVigenteDesdeDesc();
}