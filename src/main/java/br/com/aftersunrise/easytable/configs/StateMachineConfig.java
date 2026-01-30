package br.com.aftersunrise.easytable.configs;

import br.com.aftersunrise.easytable.shared.enums.PedidoStatus;
import br.com.aftersunrise.easytable.shared.enums.PedidoStatusEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

import java.util.EnumSet;

@Configuration
@EnableStateMachineFactory
public class StateMachineConfig extends EnumStateMachineConfigurerAdapter<PedidoStatus, PedidoStatusEvent> {

    @Override
    public void configure(StateMachineStateConfigurer<PedidoStatus, PedidoStatusEvent> states)
            throws Exception {
        states
                .withStates()
                .initial(PedidoStatus.PENDENTE)
                .states(EnumSet.allOf(PedidoStatus.class));
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<PedidoStatus, PedidoStatusEvent> transitions)
            throws Exception {
        transitions
                .withExternal()
                .source(PedidoStatus.PENDENTE).target(PedidoStatus.EM_PREPARACAO)
                .event(PedidoStatusEvent.INICIAR_PREPARO)
                .and()
                .withExternal()
                .source(PedidoStatus.EM_PREPARACAO).target(PedidoStatus.PRONTO)
                .event(PedidoStatusEvent.MARCAR_PRONTO)
                .and()
                .withExternal()
                .source(PedidoStatus.PRONTO).target(PedidoStatus.ENTREGUE)
                .event(PedidoStatusEvent.ENTREGAR)
                .and()
                .withExternal()
                .source(PedidoStatus.ENTREGUE).target(PedidoStatus.PAGO)
                .event(PedidoStatusEvent.CONFIRMAR_PAGAMENTO)
                .and()
                .withExternal()
                .source(PedidoStatus.PENDENTE).target(PedidoStatus.CANCELADO)
                .event(PedidoStatusEvent.CANCELAR)
                .and()
                .withExternal()
                .source(PedidoStatus.EM_PREPARACAO).target(PedidoStatus.CANCELADO)
                .event(PedidoStatusEvent.CANCELAR)
                .and()
                .withExternal()
                .source(PedidoStatus.PRONTO).target(PedidoStatus.CANCELADO)
                .event(PedidoStatusEvent.CANCELAR);
    }
}
