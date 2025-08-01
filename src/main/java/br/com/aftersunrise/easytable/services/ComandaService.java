package br.com.aftersunrise.easytable.services;

import br.com.aftersunrise.easytable.borders.entities.Comanda;
import br.com.aftersunrise.easytable.repositories.ComandaRepository;
import br.com.aftersunrise.easytable.shared.exceptions.BusinessException;
import br.com.aftersunrise.easytable.shared.models.ErrorMessage;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

@Service
public class ComandaService {
    private final ComandaRepository comandaRepository;

    public ComandaService(ComandaRepository comandaRepository) {
        this.comandaRepository = comandaRepository;
    }

    public Comanda criarNovaComanda(String mesaId) {
        Comanda comanda = new Comanda();
        comanda.setCodigoQR(UUID.randomUUID().toString());
        comanda.setMesaId(mesaId);
        comanda.setAtiva(true);
        comanda.setDataCriacao(new Date());
        return comandaRepository.save(comanda);
    }

    public Comanda validarComanda(String comandaId) {
        Comanda comanda = comandaRepository.findById(comandaId)
                .orElseThrow(() -> new BusinessException(
                        new ErrorMessage("COM404", "Comanda não encontrada"),
                        HttpStatus.NOT_FOUND
                ));
        if (!comanda.isAtiva()) {
            throw new BusinessException(
                    new ErrorMessage("COM400", "Comanda não está ativa"),
                    HttpStatus.BAD_REQUEST
            );
        }
        return comanda;
    }
}
