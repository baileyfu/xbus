package xbus.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;
import xbus.BusAspectConfiguration;


@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(BusAspectConfiguration.class)
public @interface EnableBus {
}
