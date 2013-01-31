package br.ufes.inf.lprm.scene.situation.publishing;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import br.ufes.inf.lprm.scene.situation.publishing.sinos.*;

@Target (ElementType.TYPE)
@Retention (RetentionPolicy.RUNTIME)

public @interface Publish {
	Class<? extends SituationPublisher> publisher() default SinosSituationPublisher.class;
	String host();
	int port();

}
