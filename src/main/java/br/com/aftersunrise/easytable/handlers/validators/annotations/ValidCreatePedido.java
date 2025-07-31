package br.com.aftersunrise.easytable.handlers.validators.annotations;

import br.com.aftersunrise.easytable.handlers.validators.CreatePedidoValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = CreatePedidoValidator.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidCreatePedido {
    String message() default "Requisição inválida para criação de pedido.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}