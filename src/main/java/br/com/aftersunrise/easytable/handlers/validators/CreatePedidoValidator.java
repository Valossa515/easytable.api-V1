package br.com.aftersunrise.easytable.handlers.validators;

import br.com.aftersunrise.easytable.borders.dtos.requests.CreatePedidoRequest;
import br.com.aftersunrise.easytable.handlers.validators.annotations.ValidCreatePedido;
import jakarta.validation.ConstraintValidator;

public class CreatePedidoValidator implements ConstraintValidator<ValidCreatePedido, CreatePedidoRequest> {
    @Override
    public boolean isValid(CreatePedidoRequest request, jakarta.validation.ConstraintValidatorContext context) {
        if (request == null) return false;

        boolean isValid = true;

        context.disableDefaultConstraintViolation();

        if (request.mesaId() == null || request.mesaId().isBlank()) {
            context.buildConstraintViolationWithTemplate("O campo 'mesaId' é obrigatório.")
                    .addPropertyNode("mesaId")
                    .addConstraintViolation();
            isValid = false;
        }

        if (request.itensIds() == null || request.itensIds().isEmpty()) {
            context.buildConstraintViolationWithTemplate("O pedido deve conter ao menos um item.")
                    .addPropertyNode("itensIds")
                    .addConstraintViolation();
            isValid = false;
        }

        return isValid;
    }
}
