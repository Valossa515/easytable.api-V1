package br.com.aftersunrise.easytable.services;

import br.com.aftersunrise.easytable.shared.enums.PedidoStatus;
import br.com.aftersunrise.easytable.shared.enums.PedidoStatusEvent;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;

@Service
public class PedidoStateMachineService {

    private final StateMachineFactory<PedidoStatus, PedidoStatusEvent> stateMachineFactory;

    public PedidoStateMachineService(StateMachineFactory<PedidoStatus, PedidoStatusEvent> stateMachineFactory) {
        this.stateMachineFactory = stateMachineFactory;
    }

    /**
     * Retorna o estado inicial configurado na state machine.
     */
    public PedidoStatus getEstadoInicial() {
        StateMachine<PedidoStatus, PedidoStatusEvent> stateMachine = stateMachineFactory.getStateMachine();
        stateMachine.start();
        PedidoStatus estadoInicial = stateMachine.getState().getId();
        stateMachine.stop();
        return estadoInicial;
    }

    public PedidoStatus validarTransicao(PedidoStatus statusAtual, PedidoStatusEvent evento) {
        StateMachine<PedidoStatus, PedidoStatusEvent> stateMachine = stateMachineFactory.getStateMachine();
        stateMachine.stop();
        stateMachine.getStateMachineAccessor()
                .doWithAllRegions(accessor -> accessor.resetStateMachine(
                        new DefaultStateMachineContext<>(statusAtual, null, null, null)));
        stateMachine.start();

        boolean aceitou = stateMachine.sendEvent(evento);
        if (!aceitou || stateMachine.getState() == null) {
            return null;
        }

        return stateMachine.getState().getId();
    }
}
