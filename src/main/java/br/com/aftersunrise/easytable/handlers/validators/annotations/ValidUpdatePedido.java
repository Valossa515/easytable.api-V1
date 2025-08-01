package br.com.aftersunrise.easytable.handlers.validators.annotations;

import br.com.aftersunrise.easytable.handlers.validators.CreatePedidoValidator;
import br.com.aftersunrise.easytable.handlers.validators.UpdatePedidoValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = UpdatePedidoValidator.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidUpdatePedido {
    String message() default "Requisição inválida para atualizar pedido.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
