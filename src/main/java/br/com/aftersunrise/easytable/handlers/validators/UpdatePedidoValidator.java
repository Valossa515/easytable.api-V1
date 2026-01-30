package br.com.aftersunrise.easytable.handlers.validators;

import br.com.aftersunrise.easytable.borders.dtos.requests.UpdateStatusPedidoCommand;
import br.com.aftersunrise.easytable.handlers.validators.annotations.ValidUpdatePedido;
import jakarta.validation.ConstraintValidator;

public class UpdatePedidoValidator implements ConstraintValidator<ValidUpdatePedido, UpdateStatusPedidoCommand> {
    @Override
    public boolean isValid(UpdateStatusPedidoCommand request, jakarta.validation.ConstraintValidatorContext context) {
        if (request == null) return false;

        boolean isValid = true;

        context.disableDefaultConstraintViolation();

        if (request.pedidoId() == null || request.pedidoId().isBlank()) {
            context.buildConstraintViolationWithTemplate("O campo 'pedidoId' é obrigatório.")
                    .addPropertyNode("pedidoId")
                    .addConstraintViolation();
            isValid = false;
        }

        if (request.evento() == null) {
            context.buildConstraintViolationWithTemplate("O campo 'evento' é obrigatório para transição de status via state machine.")
                    .addPropertyNode("evento")
                    .addConstraintViolation();
            isValid = false;
        }

        return isValid;
    }
}
